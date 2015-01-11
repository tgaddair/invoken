package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Shield;
import com.eldritch.invoken.encounter.Location;

public class Barrier extends Augmentation {
	private static class Holder {
        private static final Barrier INSTANCE = new Barrier();
	}
	
	public static Barrier getInstance() {
		return Holder.INSTANCE;
	}
	
    private Barrier() {
        super("barrier", true);
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ShieldAction(owner);
	}
	
	@Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ShieldAction(owner);
    }
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return true;
	}
	
	@Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }
	
	@Override
    public int getCost(Agent owner) {
        return owner.isToggled(Shield.class) ? 0 : 5;
    }
	
	@Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class ShieldAction extends AnimatedAction {
		public ShieldAction(Agent actor) {
			super(actor, Activity.Cast, Barrier.this);
		}

		@Override
		public void apply(Location location) {
			if (owner.toggle(Shield.class)) {
				owner.addEffect(new Shield(owner, Barrier.this));
			}
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
}
