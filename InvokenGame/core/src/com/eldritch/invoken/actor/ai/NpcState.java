package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.type.Npc;

public enum NpcState implements State<Npc> {
	IDLE() {
		@Override
		public void enter(Npc entity) {
			entity.getWander().setEnabled(false);
		}

		@Override
		public void update(Npc entity) {
			if (entity.getStateDuration() > 3) {
				entity.getStateMachine().changeState(PATROL);
			}
		}
	},
	
	PATROL() {
		@Override
		public void enter(Npc entity) {
			entity.getWander().setEnabled(true);
		}

		@Override
		public void update(Npc entity) {
			if (entity.getStateDuration() > 10) {
				entity.getStateMachine().changeState(IDLE);
			}
		}
	},
	
	FLEE() {
		@Override
		public void update(Npc entity) {
		}
	},
	
	FOLLOW() {
		@Override
		public void update(Npc entity) {
		}
	},
	
	ASSIST() {
		@Override
		public void update(Npc entity) {
		}
	},
	
	ATTACK() {
		@Override
		public void update(Npc entity) {
		}
	},
	
	HIDE() {
		public void update(Npc entity) {
		}
	};
	
	@Override
	public void enter(Npc entity) {
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
