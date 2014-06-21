package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Texture;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public abstract class Augmentation {
    private final Texture icon;
    private final boolean self;
	private int slots;
	private int uses;
	
	public Augmentation(String asset) {
	    this(asset, false);
	}
	
	public Augmentation(String asset, boolean self) {
	    this.icon = GameScreen.getTexture("icon/" + asset + ".png");
	    this.self = self;
	}
	
	public boolean hasEnergy(Agent agent) {
	    return agent.getInfo().getEnergy() >= getCost(agent);
	}
	
	public boolean invoke(Agent owner, Agent target) {
		if (isValid(owner, target)) {
		    Action action = getAction(owner, target);
		    if (hasEnergy(owner)) {
		        owner.addAction(action);
		        return true;
		    }
		}
		return false;
	}
	
	public Texture getIcon() {
	    return icon;
	}
	
	public boolean castsOnSelf() {
	    return self;
	}
	
	public abstract boolean isValid(Agent owner, Agent target);
	
	public abstract int getCost(Agent owner);
	
	public abstract float quality(Agent owner, Agent target, Location location);
	
	public abstract Action getAction(Agent owner, Agent target);
	
	public static Augmentation fromProto(
	        com.eldritch.scifirpg.proto.Augmentations.Augmentation proto) {
	    String className = Augmentation.class.getPackage().getName() + "." + proto.getId();
	    try {
            return (Augmentation) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            InvokenGame.error("Unable to instantiate " + className, e);
        } catch (IllegalAccessException e) {
            InvokenGame.error("Unable to access " + className, e);
        } catch (ClassNotFoundException e) {
            InvokenGame.error("Unable to find class " + className, e);
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
	    
	    @Override
	    public int getCost() {
	        return aug.getCost(owner);
	    }
	}
}
