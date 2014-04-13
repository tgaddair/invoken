package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.Action;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.effects.Shield;

public class Barrier extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new ShieldAction(owner);
	}
	
	@Override
	public boolean isValid(Agent owner, Agent target) {
		return true;
	}
	
	public class ShieldAction implements Action {
		private final Agent owner;
		
		public ShieldAction(Agent actor) {
			this.owner = actor;
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
			if (owner.toggle(Shield.class)) {
				owner.addEffect(new Shield(owner));
			}
		}
	}
}
