package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Jaunting;
import com.eldritch.invoken.effects.Jaunting.JauntHandler;
import com.eldritch.invoken.location.Level;

public class Jaunt extends SelfAugmentation {
    private static final String TOOLTIP = String.format("Jaunt\n\n"
            + "Enhances the dodge maneuver with greater force and a shockwave that stuns and "
            + "knocks back surrounding entities for a short duration.\n\n"
            + "Mode: Sustained\n"
            + "Cost: %.0f energy", JauntHandler.DODGE_COST);
    
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
    public void release(Agent owner) {
        owner.toggleOff(Jaunt.class);
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
        return owner.isToggled(Jaunt.class) ? 0 : 1;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.getInventory().hasMeleeWeapon()) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            return owner.dst2(target) > weapon.getRange() ? 2 : 0;
        }
        return 0;
    }
    
    @Override
    public String getTooltip() {
        return TOOLTIP;
    }
	
	public class JauntAction extends AnimatedAction {
		private final Vector2 target;
		
		public JauntAction(Agent actor, Vector2 target) {
			super(actor, Activity.Swipe, Jaunt.this);
			this.target = target;
		}

		@Override
		public void apply(Level level) {
		    if (owner.toggle(Jaunt.class)) {
		        owner.addEffect(new Jaunting(owner, target));
		    }
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
}
