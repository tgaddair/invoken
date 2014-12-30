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
		public void update(Npc entity, StateMachine<Npc> machine) {
			if (entity.isThreatened()) {
				entity.getStateMachine().changeState(COMBAT);
			}
		}
		
		@Override
		public void exit(Npc entity) {
			entity.getWander().setEnabled(false);
		}
	},
	
	COMBAT() {
		@Override
		public void enter(Npc entity) {
			entity.getStateMachine().getMachine(COMBAT).changeState(CombatState.ATTACK);
			
			// dispatch a message to all nearby allies that we're in combat, in the hope
			// that they'll assist us
		}
		
		@Override
		public void update(Npc entity, StateMachine<Npc> machine) {
			if (entity.isSafe()) {
				entity.getStateMachine().changeState(PATROL);
			}
		}
		
		@Override
		public void exit(Npc entity) {
			entity.getHide().setEnabled(false);
			entity.getPursue().setEnabled(false);
			entity.getEvade().setEnabled(false);
		}
	};
	
	@Override
	public void enter(Npc entity) {
	}
	
	public void update(Npc entity) {
		StateMachine<Npc> machine = getStateMachine(entity);
		machine.update();
		update(entity, machine);
	}

	@Override
	public void exit(Npc entity) {
	}

	@Override
	public boolean onMessage(Npc entity, Telegram telegram) {
		return false;
	}
	
	private StateMachine<Npc> getStateMachine(Npc entity) {
		return entity.getStateMachine().getMachine(NpcState.this);
	}
	
	protected abstract void update(Npc entity, StateMachine<Npc> machine);
}
