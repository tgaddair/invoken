package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;
import com.eldritch.invoken.actor.Inventory;

public abstract class Item {
	private final com.eldritch.scifirpg.proto.Items.Item data;
	private final float width;
	private final float height;
	
	public Item(com.eldritch.scifirpg.proto.Items.Item data, float width, float height) {
		this.data = data;
		this.width = width;
		this.height = height;
	}
	
	public abstract void equipFrom(Inventory inventory);
	
	public void render(Agent agent, Activity activity, float stateTime,
			OrthogonalTiledMapRenderer renderer) {
		TextureRegion frame = getAnimation(activity, agent.getDirection()).getKeyFrame(stateTime);
		Vector2 position = agent.getPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
	
	protected abstract Animation getAnimation(Activity activity, Direction direction);
	
	public String getId() {
		return data.getId();
	}
	
	public String getName() {
		return data.getName();
	}
	
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
