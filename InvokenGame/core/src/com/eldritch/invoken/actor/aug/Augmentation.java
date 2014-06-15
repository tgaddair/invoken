package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Texture;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public abstract class Augmentation {
    private final Texture icon;
	private int slots;
	private int uses;
	
	public Augmentation(String asset) {
	    this.icon = GameScreen.getTexture("icon/" + asset + ".png");
	}
	
	public void invoke(Agent owner, Agent target) {
		if (isValid(owner, target)) {
		    Action action = getAction(owner, target);
		    System.out.println("energy: " + owner.getInfo().getEnergy());
		    System.out.println("cost: " + action.getCost());
		    if (owner.getInfo().getEnergy() >= action.getCost()) {
		        owner.addAction(action);
		    }
		}
	}
	
	public Texture getIcon() {
	    return icon;
	}
	
	public abstract boolean isValid(Agent owner, Agent target);
	
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
}
