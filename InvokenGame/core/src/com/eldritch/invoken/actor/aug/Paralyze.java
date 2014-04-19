package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Action;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.effects.Commanded;
import com.eldritch.invoken.effects.Paralyzed;

public class Paralyze extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ParalyzeAction(owner, target);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return target != null && target != owner && target.isAlive();
	}
	
	public class ParalyzeAction implements Action {
		private final Agent owner;
		private final Agent target;
		
		public ParalyzeAction(Agent actor, Agent target) {
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
			target.addEffect(new Paralyzed(owner, target, 3));
		}
	}
}
