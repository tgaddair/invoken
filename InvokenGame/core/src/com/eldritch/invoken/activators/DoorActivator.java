package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class DoorActivator extends ClickActivator implements ProximityActivator, Crackable {
    private static final TextureRegion[] frontRegions = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-short.png", 64, 64);
    private static final TextureRegion[] frontRegionsLocked = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-short-locked.png", 64, 64);
    private static final TextureRegion[] sideRegions = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-side.png", 64, 64);
    private static final TextureRegion[] sideRegionsLocked = GameScreen.getMergedRegion(
            "sprite/activators/blast-door-side-locked.png", 64, 64);

    private final ProximityCache proximityCache = new ProximityCache(3);

    // for bounding area
    private static final int SIZE = 2;

    private final Vector2 center;
    private final LockInfo lock;
    private final Animation unlockedAnimation;
    private final Animation lockedAnimation;

    private final boolean front;
    private boolean open = false;

    private final Light light;
    private final List<Body> bodies = new ArrayList<Body>();
    private final Set<Agent> lastProximityAgents = new HashSet<>();
    private final Set<Agent> proximityAgents = new HashSet<>();
    private final Set<Agent> triggerAgents = new HashSet<>();

    private boolean activating = false;
    private boolean finished = false;
    private Optional<Boolean> lockChange = Optional.absent();
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

        final float magnitude = 0.1f;
        if (front) {
            unlockedAnimation = new Animation(0.05f, frontRegions);
            unlockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            lockedAnimation = new Animation(0.05f, frontRegionsLocked);
            lockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            center = new Vector2(x + 1, y + 0.5f);
            this.light = new StaticLight(center.cpy().add(0.5f, 0.5f), magnitude, false);
        } else {
            unlockedAnimation = new Animation(0.05f, sideRegions);
            unlockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            lockedAnimation = new Animation(0.05f, sideRegionsLocked);
            lockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            center = new Vector2(x + 0.5f, y - 1);
            this.light = new StaticLight(center.cpy().add(0.5f, 1.5f), magnitude, false);
        }

        setColor();
        lock.room.addDoor(this);
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }

    @Override
    public void update(float delta, Level level) {
        // if a single agent is in the proximity, then open the door, otherwise
        // close it
        proximityAgents.clear();
        boolean shouldOpen = false;
        for (Agent agent : level.getActiveEntities()) {
            if (inProximity(agent)) {
                shouldOpen = true;
                proximityAgents.add(agent);
                break;
            }
        }

        if (!activating) {
            if (!open && lockChange.isPresent()) {
                applyLocked(lockChange.get());
            }
        }
        if (finished) {
            postActivation(level);
        }

        // only change the state of the door if it differs from the current
        // state must click to unlock
        if (shouldOpen != open && !lock.isLocked()) {
            lastProximityAgents.removeAll(proximityAgents);
            triggerAgents.addAll(lastProximityAgents);
            setOpened(shouldOpen, level);
        }

        lastProximityAgents.clear();
        lastProximityAgents.addAll(proximityAgents);
    }

    @Override
    public void activate(Agent agent, Level level) {
        if (lock.isLocked()) {
            if (lock.canUnlock(agent)) {
                crack(agent);
            } else {
                GameScreen.toast("Requires: " + lock.getKey().getName());
            }
            return;
        }

        setOpened(!open, level);
    }

    private synchronized void setOpened(boolean opened, Level level) {
        if (activating) {
            // cannot interrupt
            return;
        }

        activating = true;
        open = opened;
        for (Body body : bodies) {
            body.setActive(!opened);
        }

        setLightWalls(level, !opened);
        InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.DOOR_OPEN, getPosition());
    }

    private void postActivation(Level level) {
        finished = false;
        if (!open) {
            onClose(triggerAgents, level);
            triggerAgents.clear();
        }
    }

    private void onClose(Set<Agent> triggerAgents, Level level) {
        for (Agent agent : triggerAgents) {
            // characters that lack this door's credentials trigger a lock in
            if (!lock.canUnlock(agent) && lock.getRoom().contains(agent.getNaturalPosition())
                    && lock.getRoom().hasHostileResident(agent)) {
                lock.getRoom().setLocked(true);
                break;
            }
        }
    }

    private void setColor() {
        if (lock.isLocked()) {
            light.setColor(1, 0, 0, 1f);
        } else {
            light.setColor(1, 1, 1, 1f);
        }
    }

    private void setLightWalls(Level level, boolean value) {
        Vector2 position = getPosition();
        float x = (int) position.x;
        float y = (int) position.y;
        if (front) {
            // add two columns for the front to prevent the flood fill from
            // going around the bottom
            level.setLightWalls((int) x - 1, (int) y + 1, (int) x + SIZE + 1, (int) y + 1, value);
        } else {
            level.setLightWalls((int) x, (int) y, (int) x, (int) y + SIZE, value);
        }
    }

    @Override
    public void register(Level level) {
        Vector2 position = getPosition();
        float x = (int) position.x;
        float y = (int) position.y;
        if (front) {
            bodies.add(level.createEdge(x, y, x + SIZE, y));
            bodies.add(level.createEdge(x, y + 1, x + SIZE, y + 1));
        } else {
            x += 0.2f;
            y -= 1;
            bodies.add(level.createEdge(x + 0.2f, y, x + 0.2f, y + SIZE));
            bodies.add(level.createEdge(x + 0.5f, y, x + 0.5f, y + SIZE));
        }
        setLightWalls(level, true);
        level.addLight(light);
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Animation animation = lock.isLocked() ? lockedAnimation : unlockedAnimation;
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                finished = true;
                activating = false;
                stateTime = 0;

                // set animations
                PlayMode lastMode = animation.getPlayMode();
                PlayMode mode = lastMode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL;
                lockedAnimation.setPlayMode(mode);
                unlockedAnimation.setPlayMode(mode);
            }
        }

        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

        Batch batch = renderer.getBatch();
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

    @Override
    public void crack(Agent source) {
        // unlock
        setLocked(false);
        // location.alertTo(agent);
    }

    public void setLocked(boolean locked) {
        lockChange = Optional.of(locked);
    }
    
    private void applyLocked(boolean locked) {
        lock.setLocked(locked);
        setColor();
    }

    @Override
    public float getStrength() {
        return lock.isLocked() ? lock.getStrength() : 0;
    }

    @Override
    public boolean isCracked() {
        return !lock.isLocked();
    }

    public static class LockInfo {
        private final Optional<Item> key;
        private final int strength;
        private final ConnectedRoom room;
        private boolean locked;

        public LockInfo(String keyId, int strength, ConnectedRoom room) {
            boolean uniqueKey = false;
            if (!Strings.isNullOrEmpty(keyId)) {
                this.key = Optional.of(Item.fromProto(InvokenGame.ITEM_READER.readAsset(keyId)));
                uniqueKey = true;
            } else {
                this.key = Optional.of(room.getKey());
            }
            this.room = room;

            // strength key:
            // 0 -> open
            // 1 -> closed
            // 2+ -> locked
            // 10 -> requires key
            this.strength = strength;
            locked = uniqueKey || strength > 1;
        }

        public ConnectedRoom getRoom() {
            return room;
        }

        public Item getKey() {
            return key.get();
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
            return !key.isPresent() || inventory.hasItem(key.get());
        }

        public static LockInfo from(ControlPoint controlPoint, ConnectedRoom room) {
            return new LockInfo(controlPoint.getRequiredKey(), controlPoint.getLockStrength(), room);
        }
    }
}
