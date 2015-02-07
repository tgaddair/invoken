package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Strings;

public class DoorActivator extends ClickActivator {
    private static final TextureRegion[] frontRegions = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-short.png", 64, 64);
    private static final TextureRegion[] frontRegionsLocked = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-short-locked.png", 64, 64);
    private static final TextureRegion[] sideRegions = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-side.png", 64, 64);
    private static final TextureRegion[] sideRegionsLocked = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-side-locked.png", 64, 64);

    // for bounding area
    private static final int SIZE = 2;

    private final LockInfo lock;
    private final Animation unlockedAnimation;
    private final Animation lockedAnimation;
    
    private final boolean front;
    private boolean open = false;

    private final List<Body> bodies = new ArrayList<Body>();

    private boolean activating = false;
    private float stateTime = 0;

    public static DoorActivator createFront(int x, int y, LockInfo lock) {
        return new DoorActivator(x, y, lock, true);
    }

    public static DoorActivator createSide(int x, int y, LockInfo lock) {
        return new DoorActivator(x, y, lock, false);
    }

    public DoorActivator(int x, int y, LockInfo lock, boolean front) {
        super(NaturalVector2.of(x, y), 2, 2);
        this.lock = lock;
        this.front = front;
        if (front) {
            unlockedAnimation = new Animation(0.05f, frontRegions);
            unlockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            lockedAnimation = new Animation(0.05f, frontRegionsLocked);
            lockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            unlockedAnimation = new Animation(0.05f, sideRegions);
            unlockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            lockedAnimation = new Animation(0.05f, sideRegionsLocked);
            lockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        }
    }

    @Override
    public void activate(Agent agent, Location location) {
        if (lock.isLocked()) {
            if (lock.canUnlock(agent)) {
                // unlock
                lock.setLocked(false);
                location.alertTo(agent);
            }
            return;
        }

        activating = true;
        open = !open;
        for (Body body : bodies) {
            body.setActive(!open);
        }
    }

    @Override
    public void register(Location location) {
        Vector2 position = getPosition();
        float x = (int) position.x;
        float y = (int) position.y;
        if (front) {
            bodies.add(location.createEdge(x, y, x + SIZE, y));
            bodies.add(location.createEdge(x, y + 1, x + SIZE, y + 1));
        } else {
            x += 0.2f;
            y -= 1;
            bodies.add(location.createEdge(x + 0.2f, y, x + 0.2f, y + SIZE));
            bodies.add(location.createEdge(x + 0.5f, y, x + 0.5f, y + SIZE));
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Animation animation = lock.isLocked() ? lockedAnimation : unlockedAnimation;
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation
                        .setPlayMode(mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
            }
        }

        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }

    @Override
    public float getZ() {
        if (open && !activating && front) {
            // always draw below everything else when open if we're a front door
            return Float.POSITIVE_INFINITY;
        }
        return getPosition().y;
    }

    public static class LockInfo {
        private final Item key;
        private final int strength;
        private boolean locked;

        public LockInfo(String keyId, int strength) {
            this.key = !Strings.isNullOrEmpty(keyId) ? Item.fromProto(InvokenGame.ITEM_READER
                    .readAsset(keyId)) : null;
            this.strength = strength;
            locked = key != null || strength > 0;
        }

        public Item getKey() {
            return key;
        }

        public int getStrength() {
            return strength;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isLocked() {
            return locked;
        }
        
        public boolean canUnlock(Agent agent) {
            return hasKey(agent.getInventory());
        }

        public boolean hasKey(Inventory inventory) {
            return inventory.hasItem(key);
        }
        
        public static LockInfo from(Encounter encounter) {
            return new LockInfo(encounter.getRequiredKey(), encounter.getLockStrength());
        }
    }
}
