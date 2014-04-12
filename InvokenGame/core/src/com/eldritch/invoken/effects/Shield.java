package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Shield {
	private final float width;
	private final float height;
	private static Animation animation;
	
	private final Agent actor;
	private float stateTime = 0;
	
	public Shield(Agent actor) {
		this.actor = actor;
		
		TextureRegion[][] regions = GameScreen.getRegions("sprite/effects/shield.png", 96, 96);
		animation = new Animation(0.15f, regions[2]);
		animation.setPlayMode(Animation.PlayMode.LOOP);
		
		width = 1 / 32f * regions[0][0].getRegionWidth();
		height = 1 / 32f * regions[0][0].getRegionHeight();
	}
	
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		stateTime += delta;
		TextureRegion frame = animation.getKeyFrame(stateTime);
		Vector2 position = actor.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
}
