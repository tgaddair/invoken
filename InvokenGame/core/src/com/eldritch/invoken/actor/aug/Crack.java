package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Cracking;
import com.eldritch.invoken.encounter.Location;

public class Crack extends Augmentation {
    public Crack() {
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
        return owner.isToggled(Crack.class) ? 0 : 5;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
    
    public class CrackAction extends AnimatedAction {
        private final boolean activate;
        
        public CrackAction(Agent actor) {
            super(actor, Activity.Cast, Crack.this);
            activate = !actor.isToggled(Crack.class);
        }

        @Override
        public void apply(Location location) {
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