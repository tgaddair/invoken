package com.eldritch.invoken.actor.aug;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.box2d.Wall;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Thrust extends ActiveAugmentation {
    private static final float RANGE = 5;
    private static final float DURATION = 0.5f;
    private static final float MAGNITUDE = 40f;
    private static final float DAMAGE_SCALE = 2f;
    private static final int BASE_COST = 25;

    private static class Holder {
        private static final Thrust INSTANCE = new Thrust();
    }

    public static Thrust getInstance() {
        return Holder.INSTANCE;
    }

    private Thrust() {
        super("thrust");
    }

    @Override
    public boolean isValid(Agent owner) {
        // only one at a time
        return !owner.isToggled(Thrust.class) && owner.getInventory().hasMeleeWeapon();
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new ThrustAction(owner, target.getPosition());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ThrustAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive()) {
            return 0;
        }

        float r = RANGE;
        return Heuristics.randomizedDistanceScore(owner.dst2(target), r * r);
    }

    public class ThrustAction extends AnimatedAction {
        private final Vector2 target;

        public ThrustAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Thrust.this);
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            owner.addEffect(new RamEffect(owner, target));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_SWING, owner.getPosition());
        }

        @Override
        public void render(OrthogonalTiledMapRenderer renderer) {
            super.render(renderer);

            // render weapon
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            if (weapon.isVisible()) {
                weapon.render(owner, Activity.Combat, getStateTime(), renderer);
            }
        }

        @Override
        protected int getApplyFrame() {
            Animation anim = owner.getAnimation(activity);
            if (anim.getKeyFrames().length == 0) {
                return 0;
            }
            return super.getApplyFrame() + 1;
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    private static class RamEffect extends BasicEffect {
        private final Vector2 targetPosition;
        private final Vector2 force = new Vector2();
        private final Vector2 deltaVector = new Vector2();
        private final Vector2 source = new Vector2();
        private boolean cancelled = false;

        public RamEffect(Agent agent, Vector2 target) {
            super(agent);
            this.targetPosition = target;
            this.force.set(target).sub(agent.getPosition()).nor().scl(MAGNITUDE);
            source.set(agent.getPosition());
        }

        public void cancel() {
            this.cancelled = true;
        }

        @Override
        protected void doApply() {
            target.toggleOn(Thrust.class);
            target.setParalyzed(true);

            float magnitude = target.getInventory().getMeleeWeapon().getDamage();
            Damage damage = Damage.from(target, DamageType.PHYSICAL,
                    (int) (magnitude * DAMAGE_SCALE));
            target.setCollisionDelegate(new RamHandler(damage, force, this));

            target.setDirection(target.getDominantDirection(force.x, force.y));
        }

        @Override
        public void dispel() {
            target.toggleOff(Thrust.class);
            target.setParalyzed(false);
            target.removeCollisionDelegate();
        }

        @Override
        public boolean isFinished() {
            return getStateTime() > DURATION || cancelled;
        }

        @Override
        protected void update(float delta) {
            deltaVector.set(targetPosition).sub(target.getPosition());
            if (source.dst2(targetPosition) > source.dst2(target.getPosition())) {
                // we haven't passed the target, so make an adjustment
                force.rotate(force.angle(deltaVector) * delta * 5);
            }
            target.applyForce(force);
        }
    }

    private static class RamHandler extends DefaultAgentHandler {
        private final Set<Agent> damaged = new HashSet<>();
        private final Damage damage;
        private final Vector2 force;
        private final RamEffect effect;

        public RamHandler(Damage damage, Vector2 force, RamEffect effect) {
            this.damage = damage;
            this.force = force.cpy().scl(25f);
            this.effect = effect;
        }

        @Override
        public boolean handle(Agent agent) {
            // avoid damaging more than once
            if (!damaged.contains(agent)) {
                Agent owner = damage.getSource();
                agent.applyForce(force);
                agent.addEffect(new Stunned(owner, agent, 0.5f));
                agent.addEffect(new Bleed(agent, damage, owner.getPosition()));
                InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_HIT, agent.getPosition());

                damaged.add(agent);
                return true;
            }

            return false;
        }

        @Override
        public boolean handle(Object userData) {
            if (userData instanceof Wall) {
                effect.cancel();
                return true;
            }
            return false;
        }
    }
}
