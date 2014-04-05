package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Entity;

public class Shield {
	static float WIDTH;
	static float HEIGHT;
	private static Texture texture;
	private static Animation animation;
	
	private final Entity actor;
	private float stateTime = 0;
	
	static {
		// load the character frames, split them, and assign them to
		// Animations
		texture = new Texture("sprite/effects/shield.png");
		TextureRegion[][] regions = TextureRegion.split(texture, 96, 96);
		
		animation = new Animation(0.15f, regions[2]);
		animation.setPlayMode(Animation.PlayMode.LOOP);
		
		WIDTH = 1 / 32f * regions[0][0].getRegionWidth();
		HEIGHT = 1 / 32f * regions[0][0].getRegionHeight();
	}
	
	public Shield(Entity actor) {
		this.actor = actor;
	}
	
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		stateTime += delta;
		TextureRegion frame = animation.getKeyFrame(stateTime);
		Vector2 position = actor.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - WIDTH / 2, position.y - HEIGHT / 2, WIDTH, HEIGHT);
		batch.end();
	}
}
