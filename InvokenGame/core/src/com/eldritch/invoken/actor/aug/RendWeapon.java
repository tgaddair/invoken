package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.encounter.Location;

public class RendWeapon extends Augmentation {
    private final Vector2 strike = new Vector2();
    
    public RendWeapon() {
        super("rend");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
    	return getAction(owner, target.getPosition());
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new RendAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && owner.getInventory().hasMeleeWeapon();
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return owner.getInventory().hasMeleeWeapon();
    }
    
    @Override
    public int getCost(Agent owner) {
        return 1;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
        return owner.dst2(target) <= weapon.getRange() ? 1 : 0;
    }

    public class RendAction extends AnimatedAction {
        private final Vector2 target;

        public RendAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, RendWeapon.this);
            this.target= target;
        }

        @Override
        public void apply(Location location) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            strike.set(owner.getPosition());
            strike.add(owner.getForwardVector().scl(weapon.getRange() / 2));
        	
            Vector2 center = getCenter(owner.getPosition(), target, weapon.getRange());
            
            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(center));
            
        	for (Agent neighbor : owner.getNeighbors()) {
        		if (neighbor.inRange(center, weapon.getRange() / 2)) {
        			neighbor.addEffect(new Bleed(owner, neighbor, weapon.getDamage()));
        		}
        	}
        }
        
        @Override
        public Vector2 getPosition() {
            return target;
        }
    }
    
    // Add half the range along the direction of the strike.  The second half of the range is the
    // radius.
    private static Vector2 getCenter(Vector2 origin, Vector2 target, float range) {
    	Vector2 delta = target.cpy().sub(origin).nor().scl(range / 2);
    	return origin.cpy().add(delta);
    }
}
