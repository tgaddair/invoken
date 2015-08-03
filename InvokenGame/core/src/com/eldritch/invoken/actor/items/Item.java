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
import com.eldritch.invoken.proto.Items.Item.Requirement;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.Utils;

public abstract class Item {
    private static final String UNKNOWN_TOOLTIP = "Visit a research station to identify encrypted items at the cost of the item's "
            + "value in fragments.";

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

    public abstract void addFrom(AgentInventory inventory);

    public abstract void equipFrom(AgentInventory inventory);

    public abstract void unequipFrom(AgentInventory inventory);
    
    public void releaseFrom(AgentInventory inventory) {
    }

    public boolean mapTo(AgentInventory inventory, int index) {
        return false;
    }

    public boolean canLoot() {
        return data.getDroppable() && !data.getHidden();
    }

    public boolean isEncrypted() {
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

    public String getName(Agent viewer) {
        if (isEncrypted() && !viewer.isIdentified(getId())) {
            return String.format("[ Unknown %s ]", getTypeName());
        }
        return getName();
    }

    public String getName() {
        return data.getName();
    }

    public double getWeight() {
        return data.getWeight();
    }

    public abstract String getTypeName();

    public int getValue() {
        return data.getValue();
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
        return data.getName();
    }

    public String getTooltipFor(Agent agent) {
        if (!agent.isIdentified(this)) {
            return String.format("%s\n\n" + "%s\n\n" + "Value: %d\n" + "Weight: %.1f", "Unknown "
                    + getTypeName(), UNKNOWN_TOOLTIP, data.getValue(), getWeight());
        }
        return getLabelString(agent);
    }

    protected String getLabelString(Agent agent) {
        return String.format("[CYAN]%s[WHITE]\n" + "%s" + "%s\n\n" + "Category: %s\n"
                + "Value: %d\n" + "Weight: %.1f", data.getName(), getRequirementsString(agent),
                data.getDescription(), getTypeName(), data.getValue(), getWeight());
    }

    protected String getRequirementsString(Agent agent) {
        StringBuilder sb = new StringBuilder();
        if (!data.getRequirementList().isEmpty()) {
            boolean first = true;
            sb.append("Requires: ");
            for (Requirement req : data.getRequirementList()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;

                String discipline = req.getDiscipline().name().substring(0, 1);
                boolean invalid = !agent.getInfo().satisfies(req);
                String reqValue = Utils.markupIfInvalid(
                        String.format("%d %s", req.getValue(), discipline), invalid);
                sb.append(reqValue);
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
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
                return Credential.from(item);
            case ICEPIK:
                return Icepik.from(item);
            case FRAGMENT:
                return Fragment.getInstance(item);
            case CORE:
                return Core.getInstance(item);
            case AMMUNITION:
                return new Ammunition(item);
            case OTHER:
            default:
                throw new IllegalArgumentException("Unrecognized Item: " + item.getType());
        }
    }
}
