package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Action;
import com.eldritch.invoken.actor.Agent;

public class Resurrect extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ResurrectAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && !target.isAlive();
	}
	
	public class ResurrectAction implements Action {
		private final Agent owner;
		private final Agent target;
		
		public ResurrectAction(Agent actor, Agent target) {
			this.owner = actor;
			this.target = target;
		}
		
		@Override
		public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		}

		@Override
		public boolean isFinished() {
			return true;
		}

		@Override
		public void apply() {
			if (!target.isAlive()) {
				target.resurrect();
				owner.addFollower(target);
			}
		}
	}
}
