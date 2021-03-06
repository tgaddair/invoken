package com.eldritch.invoken.actor.aug;

import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Augmentations.AugmentationProto;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

public abstract class Augmentation {
    private final Texture icon;

    public Augmentation(Optional<String> asset) {
        this.icon = asset.isPresent() ? GameScreen.getTexture("icon/" + asset.get() + ".png")
                : null;
    }

    public boolean hasEnergy(Agent agent) {
        return agent.getInfo().getEnergy() >= getCost(agent);
    }

    public void prepare(Agent owner) {
    }

    public void unprepare(Agent owner) {
    }

    public void release(Agent owner) {
    }

    public final Target getBestTarget(Agent owner, Agent goal, Target target) {
        target.unset();
        setBestTarget(owner, goal, target);
        return target;
    }

    protected void setBestTarget(Agent agent, Agent goal, Target target) {
        target.set(goal);
    }

    public boolean invokeOnBest(Agent owner, Agent target) {
        if (isValid(owner, target)) {
            Action action = getBestAction(owner, target);
            return invoke(owner, action);
        }
        return false;
    }

    public boolean invoke(Agent owner, Target target) {
        if (target.isValid()) {
            if (target.isAgent()) {
                return invoke(owner, target.getAgent());
            } else if (target.isLocation()) {
                return invoke(owner, target.getLocation());
            }
        }
        return false;
    }

    public boolean invoke(Agent owner, Agent target) {
        if (isValid(owner, target)) {
            Action action = getAction(owner, target);
            return invoke(owner, action);
        }
        return false;
    }

    public boolean invoke(Agent owner, Vector2 target) {
        if (isValid(owner, target)) {
            Action action = getAction(owner, target);
            return invoke(owner, action);
        }
        return false;
    }

    private boolean invoke(Agent owner, Action action) {
        if (hasEnergy(owner, action)) {
            InvokenGame.log(String.format("%s uses %s", owner.getInfo().getName(), getName()));
            owner.addAction(action);
            return true;
        }
        return false;
    }
    
    private boolean hasEnergy(Agent agent, Action action) {
        return agent.getInfo().getEnergy() >= action.getCost();
    }

    public Texture getIcon() {
        return icon;
    }
    
    public SoundEffect getFailureSound() {
        return SoundEffect.INVALID;
    }
    
    public String getLabel(Agent owner) {
        return "";
    }
    
    public String getTooltip() {
        return getName();
    }
    
    public boolean isAimed() {
        return false;
    }

    public boolean isValid(Agent owner) {
        return true;
    }

    public final boolean isValidWithAiming(Agent owner, Target target) {
        if (target.isValid()) {
            if (target.isAgent()) {
                return isValidWithAiming(owner, target.getAgent());
            } else if (target.isLocation()) {
                return isValid(owner, target.getLocation());
            }
        }
        return false;
    }

    // true if the aug is valid assuming the owner is aiming (may not be right now)
    public boolean isValidWithAiming(Agent owner, Agent target) {
        return isValid(owner, target);
    }

    public Action getBestAction(Agent owner, Agent target) {
        return getAction(owner, target);
    }
    
    public abstract void prepare(PreparedAugmentations prepared, int slot);

    public abstract boolean isValid(Agent owner, Agent target);

    public abstract boolean isValid(Agent owner, Vector2 target);

    public abstract int getCost(Agent owner);

    public abstract float quality(Agent owner, Agent target, Level level);

    public abstract Action getAction(Agent owner, Agent target);

    public abstract Action getAction(Agent owner, Vector2 target);
    
    public String getName() {
        return getClass().getSimpleName();
    }

    public AugmentationProto toProto() {
        return AugmentationProto.valueOf(getClass().getSimpleName());
    }

    public static Augmentation fromProto(AugmentationProto proto) {
        String className = Augmentation.class.getPackage().getName() + "." + proto.name();
        try {
            Object o = Class.forName(className).getMethod("getInstance").invoke(null);
            return (Augmentation) o;
        } catch (InvocationTargetException e) {
            InvokenGame.error("Unable to instantiate " + className, e);
        } catch (IllegalAccessException e) {
            InvokenGame.error("Unable to access " + className, e);
        } catch (ClassNotFoundException e) {
            InvokenGame.error("Unable to find class " + className, e);
        } catch (IllegalArgumentException e) {
            InvokenGame.error("Bad argument for " + className, e);
        } catch (NoSuchMethodException e) {
            InvokenGame.error("No method for " + className, e);
        } catch (SecurityException e) {
            InvokenGame.error("Security violation for " + className, e);
        }
        return null;
    }

    public static abstract class AugmentationAction implements Action {
        protected final Agent owner;
        private final Augmentation aug;

        public AugmentationAction(Agent owner, Augmentation aug) {
            this.owner = owner;
            this.aug = aug;
        }

        public Agent getOwner() {
            return owner;
        }

        @Override
        public int getCost() {
            return aug.getCost(owner);
        }
    }

    public static class Target {
        private final Vector2 location = new Vector2();
        private Agent agent;
        private Type type = Type.NONE;

        public void unset() {
            type = Type.NONE;
        }

        public void set(Target other) {
            this.location.set(other.location);
            this.agent = other.agent;
            this.type = other.type;
        }

        public void set(Vector2 location) {
            this.agent = null;
            this.location.set(location);
            type = Type.LOCATION;
        }

        public void set(Agent agent) {
            this.agent = agent;
            this.location.setZero();
            type = Type.AGENT;
        }

        public boolean isValid() {
            return type != Type.NONE;
        }

        public Agent getAgent() {
            return agent;
        }

        public Vector2 getLocation() {
            return location;
        }

        public boolean isLocation() {
            return type == Type.LOCATION;
        }

        public boolean isAgent() {
            return type == Type.AGENT;
        }

        private enum Type {
            NONE, AGENT, LOCATION
        }
    }
    
    public abstract static class ActiveAugmentation extends Augmentation {
        public ActiveAugmentation(String asset) {
            this(Optional.of(asset));
        }

        public ActiveAugmentation(Optional<String> asset) {
            super(asset);
        }
        
        @Override
        public void prepare(PreparedAugmentations prepared, int slot) {
            if (prepared.isActive(this, slot)) {
                // already active in this slot
                prepared.setInactive(slot);
            } else {
                prepared.setActive(this, slot);
            }
        }
    }
    
    public abstract static class SelfAugmentation extends Augmentation {
        public SelfAugmentation(String asset) {
            this(Optional.of(asset));
        }

        public SelfAugmentation(Optional<String> asset) {
            super(asset);
        }
        
        @Override
        public void prepare(PreparedAugmentations prepared, int slot) {
            boolean used = prepared.use(this);
            if (prepared.isActiveOnSelf(this)) {
                prepared.removeActiveOnSelf(this);
            } else if (used) {
                prepared.addActiveOnSelf(this);
            }
        }
    }
    
    public abstract static class InstantAugmentation extends Augmentation {
        public InstantAugmentation(String asset) {
            this(Optional.of(asset));
        }

        public InstantAugmentation(Optional<String> asset) {
            super(asset);
        }
        
        @Override
        public void prepare(PreparedAugmentations prepared, int slot) {
            prepared.use(this);
        }
    }
}
