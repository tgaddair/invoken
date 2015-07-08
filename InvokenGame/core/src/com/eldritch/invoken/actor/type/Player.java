package com.eldritch.invoken.actor.type;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.ConversationHandler.DefaultConversationHandler;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.items.Ammunition;
import com.eldritch.invoken.actor.items.Consumable;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.util.ThreatMonitor;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

/** The player character, has state and state time, */
public class Player extends SteeringAgent {
    private final Vector2 targetCoord = new Vector2();
    private final ThreatMonitor<Player> threat;
    private final String bodyType;
    private final Optional<PlayerActor> priorState;

    private boolean holding = false;
    private boolean moving = false;
    private boolean fixedTarget = false;
    private boolean lightOn = false;
    private Augmentation lastAug = null;

    private int lastFragments = 0;

    public Player(Profession profession, int level, float x, float y, Level location, String body) {
        super(x, y, Human.getWidth(), Human.getHeight(), Human.MAX_VELOCITY, profession, level,
                location, AnimationUtils.getHumanAnimations(body));
        this.threat = new ThreatMonitor<Player>(this);
        this.bodyType = body;
        this.priorState = Optional.<PlayerActor> absent();
    }

    public Player(PlayerActor data, float x, float y, Level level) {
        super(data.getParams(), true, x, y, Human.getWidth(), Human.getHeight(),
                Human.MAX_VELOCITY, level, AnimationUtils.getHumanAnimations(data.getParams()
                        .getBodyType()));

        // equip items
        Set<String> equipped = new HashSet<String>(data.getEquippedItemIdList());
        for (ItemState item : info.getInventory().getItems()) {
            if (equipped.contains(item.getItem().getId())) {
                info.getInventory().equip(item.getItem());
            }
        }
        
        // map consumable bar
        AgentInventory inv = info.getInventory();
        for (int i = 0; i < data.getConsumableIdCount(); i++) {
            String id = data.getConsumableId(i);
            if (inv.hasItem(id)) {
                Item item = inv.getItem(id);
                item.mapTo(inv, i);
            }
        }

        if (data.hasFragments()) {
            getInfo().getInventory().addItem(Fragment.getInstance(), data.getFragments());
        }

        this.threat = new ThreatMonitor<Player>(this);
        this.bodyType = data.getParams().getBodyType();

        // restore prior state
        this.priorState = Optional.of(data);
        for (String dialogue : data.getUniqueDialogueList()) {
            addDialogue(dialogue);
        }
    }

    public int getLastFragments() {
        return lastFragments;
    }

    @Override
    protected void releaseFragments() {
        int total = info.getInventory().getItemCount((Fragment.getInstance()));
        lastFragments = total;
        super.releaseFragments();
    }

    public void toggleLastAugmentation() {
        PreparedAugmentations prepared = getInfo().getAugmentations();
        if (prepared.hasActiveAugmentation(0)) {
            lastAug = prepared.getActiveAugmentation(0);
            prepared.toggleActiveAugmentation(lastAug, 0);
        } else if (lastAug != null) {
            prepared.toggleActiveAugmentation(lastAug, 0);
        }
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.HUMAN_DEATH;
    }

    @Override
    protected void takeAction(float delta, Level screen) {
        if (moving) {
            // moving = mover.takeAction(delta, targetCoord, screen);
            if (!moving) {
                fixedTarget = false;
            }
        }

        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            body.applyForceToCenter(new Vector2(-1 * getMaxLinearSpeed(), 0), true);
            moving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            body.applyForceToCenter(new Vector2(1 * getMaxLinearSpeed(), 0), true);
            moving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            body.applyForceToCenter(new Vector2(0, 1 * getMaxLinearSpeed()), true);
            moving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            body.applyForceToCenter(new Vector2(0, -1 * getMaxLinearSpeed()), true);
            moving = false;
        }
    }

    public void toggleLight() {
        lightOn = !lightOn;
    }

    public boolean hasLightOn() {
        return lightOn;
    }

    public boolean holdingPosition() {
        return holding;
    }

    public void holdPosition(boolean hold) {
        this.holding = hold;
        if (hold) {
            setMoving(false);
        }
    }

    public void moveToFixedTarget(float x, float y) {
        moveTo(x, y);
        setMoving(true);
        fixedTarget = true;
    }

    public void moveTo(float x, float y) {
        targetCoord.x = x;
        targetCoord.y = y;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean hasFixedTarget() {
        return fixedTarget;
    }

    @Override
    public void setLocation(Level level, float x, float y) {
        super.setLocation(level, x, y);
    }

    @Override
    public boolean canKeepTarget(Agent other) {
        return true;
    }

    public boolean select(Agent other, Level level) {
        // if (other == this || other == null || canTarget(other, location)) {
        setTarget(other);
        endJointInteraction();
        return true;
    }

    public void reselect(Agent other) {
        if (canInteract(other)) {
            beginInteraction(other, false);
        }
    }

    @Override
    protected void handleConfusion(boolean confused) {
        // do nothing, for now, will change to make attack at random
    }

    @Override
    protected float damage(float value) {
        if (Settings.GOD_MODE) {
            return 0;
        }
        return super.damage(value);
    }

    @Override
    public void alertTo(Agent other) {
        // does nothing
    }

    @Override
    public ThreatMonitor<?> getThreat() {
        return threat;
    }

    @Override
    public boolean canTargetProjectile(Agent other) {
        // let the player make seemingly bad shots
        return true;
    }

    @Override
    public float getCloakAlpha() {
        // players should be able to see themselves on screen even when invisible
        return 0.1f;
    }

    private boolean isTouched(float startX, float endX) {
        // check if any finger is touch the area between startX and endX
        // startX/endX are given between 0 (left edge of the screen) and 1
        // (right edge of the screen)
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX() / (float) Gdx.graphics.getWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public float getAttackSpeed() {
        return 2;
    }

    @Override
    public void recoil() {
        Vector2 shift = getPosition().cpy().sub(getLocation().getFocusPoint()).nor().scl(0.25f);
        getLocation().shiftView(shift);
    }

    @Override
    public boolean canSpeak() {
        return false;
    }

    @Override
    public ConversationHandler getDialogueHandler() {
        // not implemented
        return new DefaultConversationHandler();
    }

    public PlayerActor serialize() {
        PlayerActor.Builder builder = PlayerActor.newBuilder();
        builder.setParams(info.serialize());
        builder.getParamsBuilder().setBodyType(bodyType);

        // position
        builder.setX(getPosition().x);
        builder.setY(getPosition().y);

        // location
        Level level = getLocation();
        builder.setSeed(level.getSeed());
        builder.setRegion(level.getRegion());
        builder.setFloor(level.getFloor());

        // equipped items
        AgentInventory inventory = info.getInventory();
        if (inventory.hasOutfit()) {
            builder.addEquippedItemId(inventory.getOutfit().getId());
        }
        if (inventory.hasMeleeWeapon()) {
            builder.addEquippedItemId(inventory.getMeleeWeapon().getId());
        }
        if (inventory.hasRangedWeapon()) {
            builder.addEquippedItemId(inventory.getRangedWeapon().getId());
        }
        for (Ammunition ammo : inventory.getAmmunition()) {
            builder.addEquippedItemId(ammo.getId());
        }
        
        // consumables
        for (Consumable consumable : inventory.getConsumables()) {
            builder.addConsumableId(consumable.getId());
        }

        // carry over previous state
        if (priorState.isPresent()) {
            PlayerActor prior = priorState.get();
            builder.addAllVisitedRooms(prior.getVisitedRoomsList());
        }

        // state markers
        builder.addAllUniqueDialogue(getUniqueDialogue());

        return builder.build();
    }
}