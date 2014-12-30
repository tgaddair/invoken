package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Scrambled;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;

public class Scramble extends Augmentation {
    public Scramble() {
        super("scramble", false);
    }
    
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target.getInfo().getSpecies() == Species.AUTOMATON;
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
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class ScrambleAction extends AnimatedAction {
		private final Agent target;
		
		public ScrambleAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe, Scramble.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
			if (target.getInfo().getSpecies() == Species.AUTOMATON) {
				target.addEffect(new Scrambled(owner, target, Scramble.this, getCost()));
			}
		}
		
		@Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
	}
}