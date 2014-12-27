package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.type.Npc;

public enum PatrolState implements State<Npc> {
	WANDER() {
		@Override
		public void enter(Npc entity) {
			entity.getWander().setEnabled(true);
		}

		@Override
		public void update(Npc entity) {
			if (entity.getStateDuration() > 10) {
				entity.getStateMachine().changeState(NpcState.PATROL, IDLE);
			}
		}
	},
	
	FOLLOW() {
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
		}
	},
	
	IDLE() {
		@Override
		public void enter(Npc entity) {
			entity.getWander().setEnabled(false);
		}

		@Override
		public void update(Npc entity) {
			if (entity.getStateDuration() > 3) {
				entity.getStateMachine().changeState(NpcState.PATROL, WANDER);
			}
		}
	};

	@Override
	public void update(Npc entity) {
	}

	@Override
	public void exit(Npc entity) {
		entity.resetStateDuration();
	}

	@Override
	public boolean onMessage(Npc entity, Telegram telegram) {
		return false;
	}
}
