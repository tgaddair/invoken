package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.activators.ProximityMine;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;

public class Trap extends Augmentation {
    private static final int DAMAGE_SCALE = 25;
    
	private static class Holder {
        private static final Trap INSTANCE = new Trap();
	}
	
	public static Trap getInstance() {
		return Holder.INSTANCE;
	}
	
    private Trap() {
        super("trap", false);
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
		return new TrapAction(owner, target.getPosition());
	}
	
	@Override
	public Action getAction(Agent owner, Vector2 target) {
		return new TrapAction(owner, target);
	}
	
	@Override
    public int getCost(Agent owner) {
        return 2;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class TrapAction extends AnimatedAction {
		private final Vector2 target;
		
		public TrapAction(Agent actor, Vector2 target) {
			super(actor, Activity.Swipe, Trap.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
		    Damage damage = Damage.from(owner, DamageType.PHYSICAL, getBaseDamage(owner));
		    location.addEntity(new ProximityMine(NaturalVector2.of(target), damage));
		}
		
		@Override
        public Vector2 getPosition() {
            return target;
        }
	}
	
    private static int getBaseDamage(Agent owner) {
        return (int) (DAMAGE_SCALE * owner.getInfo().getStealthModifier());
    }
}
