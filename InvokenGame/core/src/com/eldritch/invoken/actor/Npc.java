package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.screens.GameScreen;

public class Npc extends Entity {
	public static float WIDTH;
	public static float HEIGHT;

	private static Texture playerTexture;
	private static Map<Direction, Animation> animations;
	
	private final List<Routine> routines = new ArrayList<Routine>();
	private Routine routine;

	static {
		animations = new HashMap<Direction, Animation>();
		
		// load the character frames, split them, and assign them to
		// Animations
		playerTexture = new Texture("sprite/eru_centurion/walk.png");
		TextureRegion[][] regions = TextureRegion.split(playerTexture, 48, 48);
		
		// up, left, down, right
		for (Direction d : Direction.values()) {
			Animation anim = new Animation(0.15f, regions[d.ordinal()]);
			anim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
			animations.put(d, anim);
		}

		// figure out the width and height of the player for collision
		// detection and rendering by converting a player frames pixel
		// size into world units (1 unit == 32 pixels)
		WIDTH = 1 / 32f * regions[0][0].getRegionWidth();
		HEIGHT = 1 / 32f * regions[0][0].getRegionHeight();
	}
	
	public Npc(int x, int y) {
		super(animations.get(Direction.Down), x, y);
		routine = new PatrolRoutine(this);
		routines.add(routine);
		routines.add(new IdleRoutine(this));
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
		if (routine.isFinished()) {
			Gdx.app.log(InvokenGame.LOG, "FINISHED");
			Collections.shuffle(routines);
			for (Routine r : routines) {
				if (r.isValid()) {
					routine = r;
				}
			}
		}
		routine.takeAction(delta);
	}
	
	@Override
	protected float getWidth() {
		return WIDTH;
	}
	
	@Override
	protected float getHeight() {
		return HEIGHT;
	}
	
	@Override
	protected Animation getAnimation(Direction dir) {
		return animations.get(dir);
	}
}
