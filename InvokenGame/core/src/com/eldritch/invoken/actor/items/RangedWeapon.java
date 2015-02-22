package com.eldritch.invoken.actor.items;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.eldritch.invoken.screens.GameScreen;
import com.google.common.base.Strings;

public class RangedWeapon extends Item {
    private static final float COOLDOWN = 1f;
	private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
	private final TextureRegion texture;
	
	public RangedWeapon(com.eldritch.invoken.proto.Items.Item item) {
		super(item, 18);
		if (!Strings.isNullOrEmpty(item.getAsset())) {
		    texture = new TextureRegion(GameScreen.getTexture(item.getAsset()));
		} else {
		    texture = null;
		}
	}
	
	public void render(Vector2 position, Vector2 direction, OrthogonalTiledMapRenderer renderer) {
	    if (texture == null) {
	        return;
	    }
	    
        float width = getWidth();
        float height = getHeight();
        
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(texture,
                position.x - width / 2, position.y - height / 2,  // position
                width / 2, height / 2,  // origin
                width, height,  // size
                1f, 1f,  // scale
                direction.angle());
        batch.end();    
	}
	
	public float getDamage() {
	    float damage = 0;
	    for (DamageMod mod : getData().getDamageModifierList()) {
	        damage += mod.getMagnitude();
	    }
	    return damage;
	}
	
	public float getCooldown() {
	    return COOLDOWN;
	}
	
	@Override
    public boolean isEquipped(Inventory inventory) {
        return inventory.getRangedWeapon() == this;
    }
	
	@Override
	public void equipFrom(Inventory inventory) {
		inventory.setRangedWeapon(this);
	}
	
	@Override
	public void unequipFrom(Inventory inventory) {
		if (inventory.getRangedWeapon() == this) {
			inventory.setRangedWeapon(null);
		}
	}
	
	@Override
	protected Animation getAnimation(Activity activity, Direction direction) {
		return animations.get(direction);
	}
	
	@Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("%s\n"
                + "Range: %.2f\n",
                super.toString(), data.getRange()));
        result.append("Damage:");
        for (DamageMod mod : data.getDamageModifierList()) {
            result.append(String.format("\n  %s: %d", mod.getDamage(), mod.getMagnitude()));
        }
        return result.toString();
    }
}
