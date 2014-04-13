package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
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
	private Agent followed = null;
	
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
		// update dispositions
		for (Agent agent : screen.getActors()) {
			if (agent != this && agent.isAlive() && !relations.containsKey(agent)) {
				float disp = getDisposition(agent);
				relations.put(agent, disp);
			}
		}
		
		// handle behavior for the dispositions
		for (Entry<Agent, Float> entry : relations.entrySet()) {
			// make sure we're close enough to notice this agent
			if (dst2(entry.getKey()) < 5 && entry.getValue() < 0) {
				addEnemy(entry.getKey());
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
	
	@Override
	public void setFollowing(Agent actor) {
		followed = actor;
	}
	
	public Agent getFollowed() {
		return followed;
	}
}
