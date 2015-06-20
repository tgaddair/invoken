package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Cracking;
import com.eldritch.invoken.location.Level;

public class Crack extends Augmentation {
    private static final int BASE_COST = 5;
    
	private static class Holder {
        private static final Crack INSTANCE = new Crack();
	}
	
	public static Crack getInstance() {
		return Holder.INSTANCE;
	}
	
    private Crack() {
        super("crack", true);
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new CrackAction(owner);
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new CrackAction(owner);
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
        return owner.isToggled(Crack.class) ? 0 : BASE_COST;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return 1;
    }
    
    public class CrackAction extends AnimatedAction {
        private final boolean activate;
        
        public CrackAction(Agent actor) {
            super(actor, Activity.Cast, Crack.this);
            activate = !actor.isToggled(Crack.class);
        }

        @Override
        public void apply(Level level) {
            if (activate) {
                owner.addEffect(new Cracking(owner, Crack.this, getCost()));
            } else {
                owner.toggleOff(Crack.class);
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
}