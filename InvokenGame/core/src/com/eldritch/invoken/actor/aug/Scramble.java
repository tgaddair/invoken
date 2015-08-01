package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Species;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Scrambled;
import com.eldritch.invoken.location.Level;

public class Scramble extends ActiveAugmentation {
    private static final float BASE_DURATION = 60f;
    
	private static class Holder {
        private static final Scramble INSTANCE = new Scramble();
	}
	
	public static Scramble getInstance() {
		return Holder.INSTANCE;
	}
	
    private Scramble() {
        super("scramble");
    }
    
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target.getInfo().getSpecies() == Species.Automaton;
	}

	@Override
	public boolean isValid(Agent owner, Vector2 target) {
		return false;
	}

	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ScrambleAction(owner, target);
	}
	
	@Override
	public Action getAction(Agent owner, Vector2 target) {
		return null;
	}
	
	@Override
    public int getCost(Agent owner) {
        return 2;
    }
	
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return 1;
    }
	
	public class ScrambleAction extends AnimatedAction {
		private final Agent target;
		
		public ScrambleAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe, Scramble.this);
			this.target = target;
		}

		@Override
		public void apply(Level level) {
			if (target.getInfo().getSpecies() == Species.Automaton) {
				target.addEffect(new Scrambled(owner, target, BASE_DURATION));
			}
		}
		
		@Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
	}
}
