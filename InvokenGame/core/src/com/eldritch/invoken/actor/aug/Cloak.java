package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Cloaked;
import com.eldritch.invoken.encounter.Location;

public class Cloak extends Augmentation {
    public Cloak() {
        super("cloak");
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new CloakAction(owner);
    }
    
    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        if (owner.isCloaked()) {
            // tactically, it never makes sense to turn off cloak
            return -1f;
        }
        
        float score = 0f;
        float dst2 = owner.dst2(target);
        if (dst2 < 50 && dst2 > 5) {
            score = 10f;
        }
        return score;
    }
    
    public class CloakAction extends AnimatedAction {
        private final boolean cloaked;
        
        public CloakAction(Agent actor) {
            super(actor, Activity.Cast);
            cloaked = !actor.isCloaked();
        }

        @Override
        public void apply(Location location) {
            if (cloaked) {
                owner.addEffect(new Cloaked(owner, owner, getCost()));
            } else {
                owner.setCloaked(false);
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
        
        @Override
        public int getCost() {
            return owner.isCloaked() ? 0 : 5;
        }
    }
}