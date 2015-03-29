package com.eldritch.invoken.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class Bleed extends AnimatedEffect {
	private static final List<TextureRegion[]> animations = new ArrayList<TextureRegion[]>();
	static {
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed1.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed2.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed3.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed4.png", 48, 48));
		animations.add(GameScreen.getMergedRegion("sprite/effects/bleed5.png", 48, 48));
	}
	
	private final Damage damage;
	
	public Bleed(Agent target, Damage damage) {
		super(target, randomAnimation(), new Vector2(0, 0.5f));
		this.damage = damage;
	}
	
	@Override
    protected void doApply() {
        getTarget().damage(damage);
    }
	
	@Override
    public void dispel() {
    }
	
	private static TextureRegion[] randomAnimation() {
		return animations.get((int) (Math.random() * animations.size()));
	}
}
