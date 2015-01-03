package com.eldritch.invoken.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Bleed extends AnimatedEffect {
	private static final List<TextureRegion[]> animations = new ArrayList<TextureRegion[]>();
	static {
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed1.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed2.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed3.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed4.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed5.png", 48, 48));
	}
	
	private final Agent source;
	private final float magnitude;
	
	public Bleed(Agent actor, Agent target, float magnitude) {
		super(target, randomAnimation(), new Vector2(0, 0.5f));
		this.source = actor;
		this.magnitude = magnitude;
	}
	
	@Override
    protected void doApply() {
        getTarget().damage(source, magnitude);
    }
	
	@Override
    public void dispel() {
    }
	
	private static TextureRegion[] randomAnimation() {
		return animations.get((int) (Math.random() * animations.size()));
	}
}
