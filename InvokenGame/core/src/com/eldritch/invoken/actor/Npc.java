package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.ai.AttackRoutine;
import com.eldritch.invoken.actor.ai.FollowRoutine;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.screens.GameScreen;

public class Npc extends Agent {
	private final List<Routine> routines = new ArrayList<Routine>();
	private Routine routine;
	private Agent followed = null;
	
	public Npc(int x, int y) {
		super("sprite/eru_centurion", x, y);
		routine = new IdleRoutine(this);
		
		routines.add(new AttackRoutine(this));
		routines.add(new FollowRoutine(this));
		routines.add(new PatrolRoutine(this));
		routines.add(routine); // idle is fallback
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		if (routine.isFinished()) {
			Gdx.app.log(InvokenGame.LOG, "FINISHED");
			for (Routine r : routines) {
				if (r.isValid()) {
					Gdx.app.log(InvokenGame.LOG, "routine: " + r);
					routine = r;
					break;
				}
			}
		} else {
			for (Routine r : routines) {
				if (r.canInterrupt() && r.isValid()) {
					routine = r;
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
