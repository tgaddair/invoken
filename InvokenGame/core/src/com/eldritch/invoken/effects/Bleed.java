package com.eldritch.invoken.effects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.gfx.Splash;
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
	
	private static final TextureRegion SPLASH_REGION = new TextureRegion(
            GameScreen.getTexture("sprite/effects/blood-splatter.png"));
	
	private final Damage damage;
	private final Vector2 contact;
	private final Vector2 knockback;
	
	public Bleed(Agent target, Damage damage, Vector2 contact) {
	    this(target, damage, contact, Vector2.Zero);
	}
	
	public Bleed(Agent target, Damage damage, Vector2 contact, Vector2 knockback) {
		super(target, randomAnimation(), new Vector2(0, 0.5f));
		this.contact = contact;
		this.damage = damage;
		this.knockback = knockback;
	}
	
	@Override
    protected void doApply() {
        getTarget().damage(damage, contact);
        if (!knockback.isZero()) {
            if (!target.isAlive()) {
                // extra knockback for effect
                knockback.scl(1.5f);
            }
            
            target.stop();
            target.applyForce(knockback);
        }
        target.getLocation().addEntity(new Splash(SPLASH_REGION, target.getPosition().cpy(), 3));
    }
	
	@Override
    public void dispel() {
    }
	
	private static TextureRegion[] randomAnimation() {
		return animations.get((int) (Math.random() * animations.size()));
	}
}
