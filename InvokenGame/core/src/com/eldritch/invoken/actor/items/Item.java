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
	
	public Item(com.eldritch.scifirpg.proto.Items.Item data, int px) {
		this.data = data;
		this.width = 1 / 32f * px;
		this.height = 1 / 32f * px;
	}
	
	public abstract void equipFrom(Inventory inventory);
	
	public abstract void unequipFrom(Inventory inventory);
	
	public void render(Agent agent, Activity activity, float stateTime,
			OrthogonalTiledMapRenderer renderer) {
	    if (getAnimation(activity, agent.getDirection()) == null) {
	        // not all items are rendered
	        return;
	    }
	    
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
	
	public com.eldritch.scifirpg.proto.Items.Item getData() {
		return data;
	}
	
	public static Item fromProto(com.eldritch.scifirpg.proto.Items.Item item) {
		switch (item.getType()) {
			case MELEE_WEAPON:
			    return new MeleeWeapon(item);
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
