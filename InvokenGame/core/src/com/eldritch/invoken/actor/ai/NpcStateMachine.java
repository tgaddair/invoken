package com.eldritch.invoken.actor.ai;

import java.util.EnumMap;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.eldritch.invoken.actor.type.Npc;

public class NpcStateMachine extends DefaultStateMachine<Npc> {
	private final EnumMap<NpcState, StateMachine<Npc>> machines = 
			new EnumMap<NpcState, StateMachine<Npc>>(NpcState.class);
	
	private float stateDuration = 0;

	public NpcStateMachine(Npc owner, State<Npc> initialState) {
		super(owner, initialState);
		machines.put(NpcState.COMBAT, new DefaultStateMachine<Npc>(owner, NpcState.COMBAT));
		machines.put(NpcState.PATROL, new DefaultStateMachine<Npc>(owner, PatrolState.WANDER));
	}
	
	public void update(float delta) {
		stateDuration += delta;
		update();
	}

	public StateMachine<Npc> getMachine(NpcState state) {
		return machines.get(state);
	}
	
	public void changeState(NpcState topState, State<Npc> state) {
		machines.get(topState).changeState(state);
	}
	
	public void resetStateDuration() {
		stateDuration = 0;
	}
	
	public float getStateDuration() {
		return stateDuration;
	}
	
	public Npc getOwner() {
		return owner;
	}
}
