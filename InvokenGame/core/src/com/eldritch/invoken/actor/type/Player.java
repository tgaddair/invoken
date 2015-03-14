package com.eldritch.invoken.actor.type;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.Inventory.ItemState;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.util.ThreatMonitor;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.util.Settings;

/** The player character, has state and state time, */
public class Player extends SteeringAgent {
    private final Vector2 targetCoord = new Vector2();
    private final ThreatMonitor<Player> threat;
    private boolean holding = false;
    private boolean moving = false;
    private boolean fixedTarget = false;
    private boolean lightOn = false;
    private Augmentation lastAug = null;

    public Player(Profession profession, int level, float x, float y, Location location, String body) {
        super(x, y, Human.getWidth(), Human.getHeight(), Human.MAX_VELOCITY, profession, level,
                location, Human.getAllAnimations(body));
        this.threat = new ThreatMonitor<Player>(this);
    }

    public Player(PlayerActor data, float x, float y, Location location, String body) {
        super(data.getParams(), true, x, y, Human.getWidth(), Human.getHeight(),
                Human.MAX_VELOCITY, location, Human.getAllAnimations(body));
        
        // equip items
        Set<String> equipped = new HashSet<String>(data.getEquippedItemIdList());
        for (ItemState item : info.getInventory().getItems()) {
            if (equipped.contains(item.getItem().getId())) {
                info.getInventory().equip(item.getItem());
            }
        }
        
        // set health
        info.setHealth(data.getHealth());
        this.threat = new ThreatMonitor<Player>(this);
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
    protected void takeAction(float delta, Location screen) {
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
    public void setLocation(Location location, float x, float y) {
        super.setLocation(location, x, y);
    }
    
    @Override
    public boolean canKeepTarget(Agent other) {
        return true;
    }

    public boolean select(Agent other, Location location) {
//        if (other == this || other == null || canTarget(other, location)) {
        setTarget(other);
        endInteraction();
        return true;
    }

    public void reselect(Agent other) {
        if (canInteract(other)) {
            beginDialogue(other, false);
        }
    }

    @Override
    protected void handleConfusion(boolean confused) {
        // do nothing, for now, will change to make attack at random
    }

    @Override
    public float damage(float value) {
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
        return null;
    }

    public PlayerActor serialize() {
        PlayerActor.Builder builder = PlayerActor.newBuilder();
        builder.setParams(info.serialize());
        
        // position
        builder.setX(getPosition().x);
        builder.setY(getPosition().y);
        
        // location
        builder.setSeed(getLocation().getSeed());
        builder.setLocation(getLocation().getId());
        
        // equipped items
        Inventory inventory = info.getInventory();
        if (inventory.hasOutfit()) {
            builder.addEquippedItemId(inventory.getOutfit().getId());
        }
        if (inventory.hasMeleeWeapon()) {
            builder.addEquippedItemId(inventory.getMeleeWeapon().getId());
        }
        if (inventory.hasRangedWeapon()) {
            builder.addEquippedItemId(inventory.getRangedWeapon().getId());
        }
        
        // health
        builder.setHealth(info.getHealth());
        
        return builder.build();
    }
}