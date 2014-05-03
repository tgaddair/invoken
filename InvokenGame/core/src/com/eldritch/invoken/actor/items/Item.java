package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Direction;

public abstract class Item {
	private final float width;
	private final float height;
	
	public Item(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	public void render(Agent agent, float stateTime, OrthogonalTiledMapRenderer renderer) {
		TextureRegion frame = getAnimation(agent).getKeyFrame(stateTime);
		Vector2 position = agent.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
	
	protected abstract Animation getAnimation(Agent agent);
	
	public static Item fromProto(com.eldritch.scifirpg.proto.Items.Item item) {
		switch (item.getType()) {
			case MELEE_WEAPON:
			case RANGED_WEAPON:
				return new RangedWeapon(item);
			case HEAVY_WEAPON:
			case OUTFIT:
				return new Outfit(item);
			case ACCESSORY:
			case CREDENTIAL:
			case OTHER:
			default:
				throw new IllegalArgumentException("Unrecognized Item: " + item.getType());
		}
	}
}
