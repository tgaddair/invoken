package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.Inventory;

public abstract class Item {
	protected final com.eldritch.invoken.proto.Items.Item data;
	private final float width;
	private final float height;
	
	public Item(com.eldritch.invoken.proto.Items.Item data, int px) {
		this.data = data;
		this.width = 1 / 32f * px;
		this.height = 1 / 32f * px;
	}
	
	public abstract boolean isEquipped(Inventory inventory);
	
	public abstract void equipFrom(Inventory inventory);
	
	public abstract void unequipFrom(Inventory inventory);
	
	public void render(Agent agent, Activity activity, float stateTime,
			OrthogonalTiledMapRenderer renderer) {
	    if (getAnimation(activity, agent.getDirection()) == null) {
	        // not all items are rendered
	        return;
	    }
	    
		TextureRegion frame = getAnimation(activity, agent.getDirection()).getKeyFrame(stateTime);
		Vector2 position = agent.getRenderPosition();
		
		Batch batch = renderer.getSpriteBatch();
		batch.begin();
		batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
		batch.end();
	}
	
	public void render(Agent agent, Activity activity, OrthogonalTiledMapRenderer renderer, 
	        float angle) {
	    // get direction from angle
	    Direction direction;
	    int adjusted = (int) angle - 45;
//	    System.out.println("angle: " + adjusted);
	    if (adjusted < 45) {
	        direction = Direction.Right;
	    } else if (adjusted >= 45 && adjusted < 135) {
	        direction = Direction.Up;
	        angle -= 90;
	    } else if (adjusted >= 135 && adjusted < 225) {
	        direction = Direction.Left;
	        angle -= 180;
	    } else if (adjusted >= 225 && adjusted < 315) {
	        direction = Direction.Down;
	        angle -= 270;
	    } else {
	        throw new IllegalStateException("Invalid angle: " + angle);
	    }
	    
	    if (getAnimation(activity, direction) == null) {
            // not all items are rendered
            return;
        }
        
        TextureRegion frame = getAnimation(activity, direction).getKeyFrame(0);
        Vector2 position = agent.getRenderPosition();
        
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(frame,
                position.x, position.y,  // position
                width / 2, height / 2,  // origin
                width, height,  // size
                1f, 1f,  // scale
                angle);
        batch.end();
	}
	
	protected abstract Animation getAnimation(Activity activity, Direction direction);
	
	public TextureRegion getPortrait() {
	    return getAnimation(Activity.Explore, Direction.Right).getKeyFrame(0);
	}
	
	public String getId() {
		return data.getId();
	}
	
	public String getName() {
		return data.getName();
	}
	
	public com.eldritch.invoken.proto.Items.Item getData() {
		return data;
	}
	
	public float getWidth() {
	    return width;
	}
	
	public float getHeight() {
	    return height;
	}
	
	@Override
	public String toString() {
	    return String.format("Name: %s\n"
	            + "Type: %s\n"
	            + "Description: %s\n"
	            + "Value: %d",
	            data.getName(), data.getType(), data.getDescription(), data.getValue());
	}
	
	public static Item fromProto(com.eldritch.invoken.proto.Items.Item item) {
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
