package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.effects.Jaunting;
import com.eldritch.invoken.effects.Teleported;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public class Recall extends Augmentation {
	private static class Holder {
        private static final Recall INSTANCE = new Recall();
	}
	
	public static Recall getInstance() {
		return Holder.INSTANCE;
	}
	
    private Recall() {
        super("recall", false);
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
		return getAction(owner);
	}
	
	@Override
	public Action getAction(Agent owner, Vector2 target) {
		return getAction(owner);
	}
	
	private Action getAction(Agent owner) {
	    return owner.hasMark() ? new RecallAction(owner) : new MarkAction(owner);
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
	
	public class MarkAction extends AnimatedAction {
		public MarkAction(Agent owner) {
			super(owner, Activity.Swipe, Recall.this);
		}

		@Override
		public void apply(Level level) {
		    owner.addEffect(new Marked(owner, owner.getNaturalPosition()));
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
	
	public class RecallAction extends AnimatedAction {
        public RecallAction(Agent owner) {
            super(owner, Activity.Swipe, Recall.this);
        }

        @Override
        public void apply(Level level) {
            owner.addEffect(new Teleported(owner, owner.getMark()));
            owner.resetMark();
        }
        
        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
	
	public static class Marked extends BasicEffect {
	    private final NaturalVector2 mark;
	    private float totalDistance = 0;
	    private boolean finished = false;
	    
	    public Marked(Agent owner, NaturalVector2 mark) {
	        super(owner);
	        this.mark = mark;
	        owner.setMark(mark);
	    }

	    @Override
	    public boolean isFinished() {
	        return !target.hasMark() || finished;
	    }

	    @Override
	    public void dispel() {
	        target.getInfo().changeMaxEnergy(getEnergy(totalDistance));
	    }
	    
	    @Override
	    protected void doApply() {
	    }

	    @Override
	    protected void update(float delta) {
	        NaturalVector2 current = target.getNaturalPosition();
	        float distance = current.mdst(mark);
	        float change = distance - totalDistance;
	        target.getInfo().changeMaxEnergy(-getEnergy(change));
	        totalDistance = distance;
	    }
	    
	    private float getEnergy(float mdst) {
	        return 1f * mdst;
	    }
	}

}
