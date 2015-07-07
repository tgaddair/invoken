package com.eldritch.invoken.actor.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Items;
import com.eldritch.invoken.util.Settings;

public abstract class Item {
    protected final Items.Item data;
    private final float width;
    private final float height;

    public Item(Items.Item data, int px) {
        this(data, px * Settings.SCALE, px * Settings.SCALE);
    }

    public Item(Items.Item data, TextureRegion region) {
        this(data, region != null ? region.getRegionWidth() * Settings.SCALE : 0,
                region != null ? region.getRegionHeight() * Settings.SCALE : 0);
    }

    public Item(Items.Item data, float width, float height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public void equipIfBetter(AgentInventory inventory) {
        equipFrom(inventory);
    }

    public abstract boolean isEquipped(AgentInventory inventory);

    public abstract void equipFrom(AgentInventory inventory);

    public abstract void unequipFrom(AgentInventory inventory);

    public boolean mapTo(AgentInventory inventory, int index) {
        return false;
    }

    public void render(Agent agent, Activity activity, float stateTime,
            OrthogonalTiledMapRenderer renderer) {
        if (getAnimation(activity, agent.getDirection()) == null) {
            // not all items are rendered
            return;
        }

        TextureRegion frame = getAnimation(activity, agent.getDirection()).getKeyFrame(stateTime);
        Vector2 position = agent.getRenderPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
        batch.end();
    }

    protected abstract Animation getAnimation(Activity activity, Direction direction);

    public TextureRegion getPortrait() {
        return getAnimation(Activity.Idle, Direction.Right).getKeyFrame(0);
    }

    public String getId() {
        return data.getId();
    }

    public String getName() {
        return data.getName();
    }

    public Items.Item getData() {
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
        return String.format("Name: %s\n" + "Type: %s\n" + "Description: %s\n" + "Value: %d",
                data.getName(), data.getType(), data.getDescription(), data.getValue());
    }

    public static Item fromProto(Items.Item item) {
        switch (item.getType()) {
            case MELEE_WEAPON:
                return MeleeWeapon.from(item);
            case RANGED_WEAPON:
                return RangedWeapon.from(item);
            case HEAVY_WEAPON:
            case OUTFIT:
                return new Outfit(item);
            case CONSUMABLE:
                return new Consumable(item);
            case ACCESSORY:
            case CREDENTIAL:
                return new Credential(item);
            case FRAGMENT:
                return Fragment.getInstance(item);
            case AMMUNITION:
                return new Ammunition(item);
            case OTHER:
            default:
                throw new IllegalArgumentException("Unrecognized Item: " + item.getType());
        }
    }
}
