package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.ai.StateValidator.TimedValidator;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public enum PatrolState implements State<Npc> {
	WANDER() {
		@Override
		public void enter(Npc entity) {
			entity.getStateMachine().setValidator(new TimedValidator((float) Math.random() * 10));
			entity.getWander().setEnabled(true);
		}

		@Override
		public void update(Npc entity) {
			if (entity.isFollowing()) {
				entity.getStateMachine().changeState(NpcState.PATROL, FOLLOW);
			} else if (!entity.getStateMachine().getValidator().isValid()) {
				entity.getStateMachine().changeState(NpcState.PATROL, IDLE);
			}
		}
		
		@Override
		protected void afterExit(Npc entity) {
			entity.getWander().setEnabled(false);
		}
	},
	
	FOLLOW() {
		@Override
		public void enter(Npc entity) {
			if (entity.isFollowing()) {
				entity.getArrive().setTarget(entity.getFollowed());
				entity.getArrive().setEnabled(true);
			}
		}

		@Override
		public void update(Npc entity) {
			if (!entity.isFollowing()) {
				// lost our target
				entity.getStateMachine().changeState(NpcState.PATROL, WANDER);
				return;
			}
			
			Agent followed = entity.getFollowed();
			if (followed != entity.getArrive().getTarget()) {
				// new target
				entity.getArrive().setTarget(followed);
			}
		}
		
		@Override
		protected void afterExit(Npc entity) {
			entity.getArrive().setEnabled(false);
		}
	},
	
	IDLE() {
		@Override
		public void enter(Npc entity) {
			entity.getStateMachine().setValidator(new TimedValidator((float) Math.random() * 10));
			entity.getWander().setEnabled(false);
		}

		@Override
		public void update(Npc entity) {
			if (entity.isFollowing()) {
				entity.getStateMachine().changeState(NpcState.PATROL, FOLLOW);
			} else if (!entity.getStateMachine().getValidator().isValid()) {
				entity.getStateMachine().changeState(NpcState.PATROL, WANDER);
			}
		}
	};

	@Override
	public void update(Npc entity) {
	}

	@Override
	public void exit(Npc entity) {
		entity.getStateMachine().resetValidator();
		afterExit(entity);
	}

	@Override
	public boolean onMessage(Npc entity, Telegram telegram) {
		return false;
	}
	
	protected void afterExit(Npc entity) {
	}
}
