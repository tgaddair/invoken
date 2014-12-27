package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.type.Npc;

public enum CombatState implements State<Npc> {
	ATTACK() {
		@Override
		public void enter(Npc entity) {
			entity.getSeek().setEnabled(true);
		}

		@Override
		public void update(Npc entity) {
		}
		
		@Override
		public void exit(Npc entity) {
			entity.getSeek().setEnabled(false);
		}
	},
	
	ASSIST() {
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
		}
	},
	
	FLEE() {
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
		}
	},
	
	HIDE() {
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
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
