package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.ActivatedEffect;
import com.eldritch.invoken.location.Level;

public class Empathy extends ActiveAugmentation {
	private static class Holder {
        private static final Empathy INSTANCE = new Empathy();
	}
	
	public static Empathy getInstance() {
		return Holder.INSTANCE;
	}
	
    private Empathy() {
        super("observe");
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new EmpathizeAction(owner, target);
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
        return owner.isToggled(Empathy.class);
    }
    
    @Override
    public int getCost(Agent owner) {
        return owner.isToggled(Empathy.class) ? 0 : 1;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return 1;
    }
    
    public class EmpathizeAction extends AnimatedAction {
        private final Agent target;
        private final boolean activate;
        
        public EmpathizeAction(Agent actor, Agent target) {
            super(actor, Activity.Swipe, Empathy.this);
            this.target = target;
            activate = !actor.isToggled(Empathy.class);
        }

        @Override
        public void apply(Level level) {
            if (activate) {
                owner.addEffect(new Empathizing(owner, target, Empathy.this, getCost()));
            } else {
                owner.toggleOff(Empathy.class);
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
    
    public class Empathizing extends ActivatedEffect<Empathy> {
        public Empathizing(Agent target, Agent observed, Empathy aug, int cost) {
            super(target, aug, Empathy.class, cost);
        }

        @Override
        protected void afterApply() {
        }

        @Override
        protected void afterDispel() {
        }
    }
}