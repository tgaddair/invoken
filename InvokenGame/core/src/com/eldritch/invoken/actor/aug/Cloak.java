package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Cloaked;
import com.eldritch.invoken.location.Level;

public class Cloak extends Augmentation {
    private static final int BASE_COST = 3;
    
	private static class Holder {
        private static final Cloak INSTANCE = new Cloak();
	}
	
	public static Cloak getInstance() {
		return Holder.INSTANCE;
	}
	
    private Cloak() {
        super("cloak", true);
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new CloakAction(owner);
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new CloakAction(owner);
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
        return owner.isCloaked() ? 0 : BASE_COST;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        float dst2 = owner.dst2(target);
        if (owner.isCloaked()) {
            if (dst2 > 70) {
                return 10f;
            }
            return -1f;
        }
        
        float score = 0f;
        if (dst2 < 50 && dst2 > 5) {
            score = 10f;
        }
        return score;
    }
    
    public class CloakAction extends AnimatedAction {
        private final boolean cloaked;
        
        public CloakAction(Agent actor) {
            super(actor, Activity.Cast, Cloak.this);
            cloaked = !actor.isCloaked();
        }

        @Override
        public void apply(Level level) {
            if (cloaked) {
                owner.addEffect(new Cloaked(owner, Cloak.this, getCost()));
            } else {
                owner.setCloaked(false);
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
}