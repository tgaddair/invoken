package com.eldritch.invoken.actor.items;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;

public class Outfit extends Item {
	private final Map<Activity, Map<Direction, Animation>> animations;
	
	public Outfit(com.eldritch.scifirpg.proto.Items.Item item) {
		super(item, Human.PX);
		animations = Human.getAllAnimations(getAssetPath(item.getAsset()));
	}
	
	public boolean covers() {
		return getData().getCovers();
	}
	
	@Override
    public boolean isEquipped(Inventory inventory) {
        return inventory.getOutfit() == this;
    }
	
	@Override
	public void equipFrom(Inventory inventory) {
		inventory.setOutfit(this);
	}
	
	@Override
	public void unequipFrom(Inventory inventory) {
		if (inventory.getOutfit() == this) {
			inventory.setOutfit(null);
		}
	}
	
	@Override
	protected Animation getAnimation(Activity activity, Direction direction) {
		return animations.get(activity).get(direction);
	}
	
	private static String getAssetPath(String basename) {
		return String.format("sprite/items/outfits/%s.png", basename);
	}
}
