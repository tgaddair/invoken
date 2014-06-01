package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.effects.Paralyzed;
import com.eldritch.invoken.encounter.Location;

public class Paralyze extends Augmentation {
    public Paralyze() {
        super("paralyze");
    }
    
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ParalyzeAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	public class ParalyzeAction extends AnimatedAction {
		private final Agent target;
		
		public ParalyzeAction(Agent actor, Agent target) {
			super(actor, Activity.Swipe);
			this.target = target;
		}

		@Override
		public void apply(Location location) {
			target.addEffect(new Paralyzed(owner, target, 3));
		}
	}
}
