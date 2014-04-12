package com.eldritch.invoken.actor.weapon;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Direction;

public class Shotgun {
	private final float width;
	private final float height;
	private static Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	
	private final Agent actor;
	
	public Shotgun(Agent actor) {
		this.actor = actor;
		animations = Agent.getAnimations("sprite/weapons/shotgun.png");
		
		width = 1 / 32f * 48;
		height = 1 / 32f * 48;
	}
	
	public void render(int index, OrthogonalTiledMapRenderer renderer) {
		TextureRegion frame = getAnimation().getKeyFrames()[index];
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
