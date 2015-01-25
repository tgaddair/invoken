package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Observing;
import com.eldritch.invoken.encounter.Location;

public class Observe extends Augmentation {
	private static class Holder {
        private static final Observe INSTANCE = new Observe();
	}
	
	public static Observe getInstance() {
		return Holder.INSTANCE;
	}
	
    private Observe() {
        super("observe", false);
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new ObserveAction(owner, target);
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return getAction(owner, owner);
    }
    
    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        // only to dispel
        return owner.isToggled(Observe.class);
    }
    
    @Override
    public int getCost(Agent owner) {
        return owner.isToggled(Observe.class) ? 0 : 1;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
    
    public class ObserveAction extends AnimatedAction {
        private final Agent target;
        private final boolean activate;
        
        public ObserveAction(Agent actor, Agent target) {
            super(actor, Activity.Swipe, Observe.this);
            this.target = target;
            activate = !actor.isToggled(Observe.class);
        }

        @Override
        public void apply(Location location) {
            if (activate) {
                owner.addEffect(new Observing(owner, target, Observe.this, getCost()));
            } else {
                owner.toggleOff(Observe.class);
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
}