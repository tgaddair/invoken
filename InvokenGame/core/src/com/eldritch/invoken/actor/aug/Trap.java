package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.activators.ProximityMine;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.DamagedEnergy;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Condition;
import com.eldritch.invoken.util.Damage;

public class Trap extends Augmentation {
    private static final int DAMAGE_SCALE = 100;
    private static final float MAX_DST2 = 1f;
    private static final float COST = -10f;
    
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
    public boolean isValid(Agent owner) {
        return owner.getInfo().getEnergy() >= Math.abs(COST);
    }
    
	@Override
	public boolean isValid(Agent owner, Agent target) {
	    return false;
	}

	@Override
	public boolean isValid(Agent owner, Vector2 target) {
	    return isValid(owner) 
	            && owner.getLocation().isGround(NaturalVector2.of(target)) 
	            && owner.getPosition().dst2(target) < MAX_DST2;
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
		    final ProximityMine mine = new ProximityMine(target, damage, COST);
		    location.addEntity(mine);
		    location.addActivator(mine);
		    
		    // having a proximity mine imposes an energy cost on the invocator
		    getOwner().addEffect(new DamagedEnergy(getOwner(), COST, new Condition() {
                @Override
                public boolean satisfied() {
                    return mine.isFinished();
                }
		    }));
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
