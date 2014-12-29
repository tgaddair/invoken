package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.type.Npc;

public enum NpcState implements State<Npc> {
	PATROL() {
		@Override
		public void enter(Npc entity) {
			entity.getStateMachine().getMachine(PATROL).changeState(PatrolState.WANDER);
		}

		@Override
		public void update(Npc entity) {
			StateMachine<Npc> machine = entity.getStateMachine().getMachine(PATROL);
			machine.update();
			
			if (entity.isThreatened()) {
				entity.getStateMachine().changeState(COMBAT);
			}
		}
	},
	
	COMBAT() {
		@Override
		public void enter(Npc entity) {
			entity.getStateMachine().getMachine(COMBAT).changeState(CombatState.ATTACK);
		}
		
		@Override
		public void update(Npc entity) {
			StateMachine<Npc> machine = entity.getStateMachine().getMachine(COMBAT);
			machine.update();
			
			if (entity.isSafe()) {
				entity.getStateMachine().changeState(PATROL);
			}
		}
	};
	
	@Override
	public void enter(Npc entity) {
	}

	@Override
	public void exit(Npc entity) {
	}

	@Override
	public boolean onMessage(Npc entity, Telegram telegram) {
		return false;
	}
}
