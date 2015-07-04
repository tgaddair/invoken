package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.effects.Cloaked;
import com.eldritch.invoken.location.Level;

/**
 * Target follows the invocator until effect is cancelled.  Drains the further the target strays
 * from their planned destination.  Cost is reduced as disposition towards the invocator increases.
 * Only effective on non-hostiles.
 */
public class Lead extends SelfAugmentation {
    private static final int BASE_COST = 3;
    
	private static class Holder {
        private static final Lead INSTANCE = new Lead();
	}
	
	public static Lead getInstance() {
		return Holder.INSTANCE;
	}
	
    private Lead() {
        super("lead");
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new LeadAction(owner);
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new LeadAction(owner);
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
    
    public class LeadAction extends AnimatedAction {
        private final boolean cloaked;
        
        public LeadAction(Agent actor) {
            super(actor, Activity.Cast, Lead.this);
            cloaked = !actor.isCloaked();
        }

        @Override
        public void apply(Level level) {
            if (cloaked) {
                owner.addEffect(new Cloaked(owner, Lead.this, getCost()));
            } else {
                owner.setCloaked(false);
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
    
    public class Leading extends BasicEffect {
        private final Augmentation aug;
        private final int cost;
        private boolean finished = false;

        public Leading(Agent target, Augmentation aug, int cost) {
            super(target);
            this.aug = aug;
            this.cost = cost;
        }

        @Override
        protected void doApply() {
            target.setCloaked(true);
            target.setStunted(true); // cannot regain energy
        }

        @Override
        public void dispel() {
            target.setCloaked(false);
            target.setStunted(false);
            target.getInfo().getAugmentations().removeSelfAugmentation(aug);
        }

        @Override
        public boolean isFinished() {
            return finished || (isApplied() && !target.isCloaked());
        }

        @Override
        protected void update(float delta) {
            if (target.getVelocity().isZero()) {
                // don't bother draining energy while not moving
                return;
            }

            // drains continuously while moving
            float c = cost * delta;
            if (c <= target.getInfo().getEnergy()) {
                target.getInfo().expend(c);
            } else {
                finished = true;
            }
        }
    }
}