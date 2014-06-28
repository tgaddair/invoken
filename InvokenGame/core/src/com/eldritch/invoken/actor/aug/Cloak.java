package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Cloaked;
import com.eldritch.invoken.encounter.Location;

public class Cloak extends Augmentation {
    public Cloak() {
        super("cloak", true);
    }
    
    @Override
    public Action getAction(Agent owner, Agent target, Vector2 position) {
        return new CloakAction(owner);
    }
    
    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }
    
    @Override
    public int getCost(Agent owner) {
        return owner.isCloaked() ? 0 : 5;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
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
        public void apply(Location location) {
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