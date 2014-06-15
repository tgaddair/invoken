package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Shield;
import com.eldritch.invoken.encounter.Location;

public class Barrier extends Augmentation {
    public Barrier() {
        super("barrier");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ShieldAction(owner);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return true;
	}
	
	@Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class ShieldAction extends AnimatedAction {
		public ShieldAction(Agent actor) {
			super(actor, Activity.Cast);
		}

		@Override
		public void apply(Location location) {
			if (owner.toggle(Shield.class)) {
				owner.addEffect(new Shield(owner));
			}
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }

        @Override
        public int getCost() {
            return owner.isToggled(Shield.class) ? 0 : 5;
        }
	}
}
