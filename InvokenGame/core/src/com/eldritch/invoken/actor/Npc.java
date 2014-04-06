package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.screens.GameScreen;

public class Npc extends AnimatedEntity {
	private final List<Routine> routines = new ArrayList<Routine>();
	private Routine routine;
	
	public Npc(int x, int y) {
		super("sprite/eru_centurion", x, y);
		routine = new PatrolRoutine(this);
		routines.add(routine);
		routines.add(new IdleRoutine(this));
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		if (routine.isFinished()) {
			//Gdx.app.log(InvokenGame.LOG, "FINISHED");
			Collections.shuffle(routines);
			for (Routine r : routines) {
				if (r.isValid()) {
					routine = r;
				}
			}
		}
		routine.takeAction(delta);
	}
}
