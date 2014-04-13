package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.ai.AttackRoutine;
import com.eldritch.invoken.actor.ai.FollowRoutine;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.actor.aug.FireWeapon;
import com.eldritch.invoken.screens.GameScreen;

public class Npc extends Agent {
	private final Map<Agent, Float> relations = new HashMap<Agent, Float>();
	private final List<Routine> routines = new ArrayList<Routine>();
	private Routine routine;
	
	public Npc(int x, int y) {
		super("sprite/eru_centurion", x, y);
		
		// routines
		routine = new IdleRoutine(this);
		routines.add(new AttackRoutine(this));
		routines.add(new FollowRoutine(this));
		routines.add(new PatrolRoutine(this));
		routines.add(routine); // idle is fallback
		
		// debug augs
		addAugmentation(new FireWeapon());
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		// cleanup state
		Iterator<Entry<Agent, Float>> it = relations.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Agent, Float> entry = it.next();
			if (!entry.getKey().isAlive()) {
				it.remove();
			}
		}
		
		// update dispositions for neighbors
		for (Agent agent : screen.getActors()) {
			if (dst2(agent) < 50) {
				if (agent != this && agent.isAlive()) {
					if (!relations.containsKey(agent)) {
						relations.put(agent, getDisposition(agent));
					}
					
					// add enemies and allies
					if (relations.get(agent) < 0) {
						addEnemy(agent);
					}
				}
			}
		}
		
		if (routine.isFinished()) {
//			Gdx.app.log(InvokenGame.LOG, "FINISHED");
			for (Routine r : routines) {
				if (r.isValid()) {
//					Gdx.app.log(InvokenGame.LOG, "routine: " + r);
					routine = r;
					routine.reset();
					break;
				}
			}
		} else {
			for (Routine r : routines) {
				if (r.canInterrupt() && r.isValid()) {
					if (r != routine) {
						routine = r;
						routine.reset();
					}
					break;
				}
			}
		}
		routine.takeAction(delta, screen);
	}
	
	public Map<Agent, Float> getRelations() {
		return relations;
	}
	
	@Override
	protected void onDeath() {
		super.onDeath();
		relations.clear();
	}
}
