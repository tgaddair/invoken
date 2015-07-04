package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.InstantAugmentation;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Jaunting;
import com.eldritch.invoken.location.Level;

public class Jaunt extends InstantAugmentation {
	private static class Holder {
        private static final Jaunt INSTANCE = new Jaunt();
	}
	
	public static Jaunt getInstance() {
		return Holder.INSTANCE;
	}
	
    private Jaunt() {
        super("jaunt");
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
	public Action getAction(Agent owner, Agent target) {
	    Vector2 position;
        if (target == null || owner == target) {
            if (owner.isAiming()) {
                position = owner.getFocusPoint();
            } else {
                position = owner.getForwardVector().scl(5f).add(owner.getPosition());
            }
        } else {
            position = target.getPosition();
        }
		return new JauntAction(owner, position);
	}
	
	@Override
	public Action getAction(Agent owner, Vector2 target) {
		return new JauntAction(owner, target);
	}
	
	@Override
    public int getCost(Agent owner) {
        return 2;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.getInventory().hasMeleeWeapon()) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            return owner.dst2(target) > weapon.getRange() ? 2 : 0;
        }
        return 0;
    }
	
	public class JauntAction extends AnimatedAction {
		private final Vector2 target;
		
		public JauntAction(Agent actor, Vector2 target) {
			super(actor, Activity.Swipe, Jaunt.this);
			this.target = target;
		}

		@Override
		public void apply(Level level) {
			owner.addEffect(new Jaunting(owner, target));
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
}
