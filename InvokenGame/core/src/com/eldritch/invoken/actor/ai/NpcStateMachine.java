package com.eldritch.invoken.actor.ai;

import java.util.EnumMap;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.eldritch.invoken.actor.type.Npc;

public class NpcStateMachine extends DefaultStateMachine<Npc> {
	private final EnumMap<NpcState, StateMachine<Npc>> machines = 
			new EnumMap<NpcState, StateMachine<Npc>>(NpcState.class);

	public NpcStateMachine(Npc owner, State<Npc> initialState) {
		super(owner, initialState);
		machines.put(NpcState.COMBAT, new DefaultStateMachine<Npc>(owner, NpcState.COMBAT));
		machines.put(NpcState.PATROL, new DefaultStateMachine<Npc>(owner, PatrolState.WANDER));
	}

	public StateMachine<Npc> getMachine(NpcState state) {
		return machines.get(state);
	}
	
	public void changeState(NpcState topState, State<Npc> state) {
		machines.get(topState).changeState(state);
	}
}
