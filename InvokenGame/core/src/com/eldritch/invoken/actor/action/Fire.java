package com.eldritch.invoken.actor.action;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AnimatedEntity;
import com.eldritch.invoken.actor.AnimatedEntity.Direction;
import com.eldritch.invoken.screens.GameScreen;

public class Fire {
	private final float width;
	private final float height;
	private static Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	
	private final AnimatedEntity actor;
	private float stateTime = 0;
	
	public Fire(AnimatedEntity actor) {
		this.actor = actor;
		
		TextureRegion[][] regions = GameScreen.getRegions(
				"sprite/effects/muzzle-flash.png", 96, 96);
		for (Direction d : Direction.values()) {
			Animation anim = new Animation(0.15f, regions[d.ordinal()]);
			anim.setPlayMode(Animation.PlayMode.NORMAL);
			animations.put(d, anim);
		}
		
		width = 1 / 32f * regions[0][0].getRegionWidth();
		height = 1 / 32f * regions[0][0].getRegionHeight();
	}
	
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		stateTime += delta;
		TextureRegion frame = getAnimation().getKeyFrame(stateTime);
		Vector2 position = actor.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
	
	private Animation getAnimation() {
		return animations.get(actor.getDirection());
	}
}
