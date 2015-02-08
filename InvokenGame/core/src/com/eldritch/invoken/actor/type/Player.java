package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.util.Settings;

/** The player character, has state and state time, */
public class Player extends SteeringAgent {
    private final Vector2 targetCoord = new Vector2();
    private boolean holding = false;
    private boolean moving = false;
    private boolean fixedTarget = false;
    private boolean lightOn = false;
    private Augmentation lastAug = null;

    public Player(Profession profession, int level, float x, float y, Location location, String body) {
        super(x, y, Human.getWidth(), Human.getHeight(), Human.MAX_VELOCITY, profession, level, location, 
        		Human.getAllAnimations(body));
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
//            moving = mover.takeAction(delta, targetCoord, screen);
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

    public boolean select(Agent other, Location location) {
        if (other == this || other == null || canTarget(other, location)) {
            setTarget(other);
            endInteraction();
            return true;
        }
        return false;
    }

    public void reselect(Agent other) {
        if (canInteract(other)) {
            interact(other);
            other.interact(this);
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
    public boolean canSpeak() {
        return false;
    }

    @Override
    public ConversationHandler getDialogueHandler() {
        // not implemented
        return null;
    }
}