package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Jaunting;
import com.eldritch.invoken.effects.Scrambled;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;

public class Jaunt extends Augmentation {
    public Jaunt() {
        super("jaunt", false);
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
		return new JauntAction(owner, target.getPosition());
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
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }
	
	public class JauntAction extends AnimatedAction {
		private final Vector2 target;
		
		public JauntAction(Agent actor, Vector2 target) {
			super(actor, Activity.Swipe, Jaunt.this);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
			owner.addEffect(new Jaunting(owner, target));
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
}
