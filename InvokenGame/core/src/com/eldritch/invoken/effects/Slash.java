package com.eldritch.invoken.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Slash extends AnimatedEffect {
	private static final List<TextureRegion[]> animations = new ArrayList<TextureRegion[]>();
	static {
		animations.add(GameScreen.getMergedRegion(
				format("slash01"), format("slash02"), format("slash03"), format("slash04")));
	}
	
	public Slash(Agent target, Vector2 strike) {
		super(target, randomAnimation(),
				new Vector2(target.getPosition().x - strike.x, target.getPosition().y - strike.y).nor(),
				strike.cpy().sub(target.getPosition()).angle(),
				Animation.PlayMode.NORMAL, 0.035f);
		;
	}
	
	@Override
    protected void doApply() {
    }
	
	@Override
    public void dispel() {
    }
	
	private static TextureRegion[] randomAnimation() {
		return animations.get((int) (Math.random() * animations.size()));
	}
	
	private static String format(String prefix) {
		return "sprite/effects/slash/" + prefix + ".png";
	}
}
