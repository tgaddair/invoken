package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.DamageHandler;
import com.eldritch.invoken.actor.items.Icepik;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.AgentListener;
import com.eldritch.invoken.actor.util.Damageable;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.gfx.WorldText;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class DoorActivator extends ClickActivator implements Crackable, Damageable, AgentListener {
    private static final Texture PADLOCK = GameScreen.getTexture("icon/padlock.png");
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

    private final DoorBulletHandler bulletHandler;

    private final boolean front;
    private boolean open = false;

    private final Light light;
    private final List<Body> bodies = new ArrayList<>();
    private final Set<Agent> residents = new HashSet<>();

    private Level level = null;
    private HealthBar healthBar = null;
    private boolean activating = false;
    private boolean finished = false;
    private Optional<Boolean> lockChange = Optional.absent();
    private float stateTime = 0;

    public static DoorActivator createFront(int x, int y, LockInfo lock) {
        Vector2 center = new Vector2(x + 1, y + 0.5f);
        return new DoorActivator(x, y, center, lock, true);
    }

    public static DoorActivator createSide(int x, int y, LockInfo lock) {
        Vector2 center = new Vector2(x + 0.5f, y - 1);
        return new DoorActivator(x, y, center, lock, false);
    }

    public DoorActivator(int x, int y, Vector2 center, LockInfo lock, boolean front) {
        super(x, y, front ? 1 : 1, 2, ProximityParams.of(center).withIndicator(
                createIndicator(lock, front)));
        this.bulletHandler = new DoorBulletHandler();
        this.lock = lock;
        this.front = front;

        final float magnitude = 0.1f;
        if (front) {
            unlockedAnimation = new Animation(0.05f, frontRegions);
            unlockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            lockedAnimation = new Animation(0.05f, frontRegionsLocked);
            lockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            this.light = new StaticLight(center.cpy().add(0.5f, 0.5f), magnitude, false);
        } else {
            unlockedAnimation = new Animation(0.05f, sideRegions);
            unlockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            lockedAnimation = new Animation(0.05f, sideRegionsLocked);
            lockedAnimation.setPlayMode(Animation.PlayMode.NORMAL);
            this.light = new StaticLight(center.cpy().add(0.5f, 1.5f), magnitude, false);
        }

        setColor();

        lock.room.addDoor(this);
    }

    @Override
    public void update(float delta, Level level) {
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
        super.update(delta, level);
    }

    @Override
    protected boolean shouldActivate(boolean hasProximity) {
        return super.shouldActivate(hasProximity) && !lock.isLocked();
    }

    @Override
    protected boolean onProximityChange(boolean hasProximity, Level level) {
        return setOpened(hasProximity, level);
    }

    @Override
    public void activate(Agent agent, Level level) {
        if (lock.isLocked()) {
            if (lock.canUnlock(agent)) {
                crack(agent);
            } else if (lock.canPick(agent)) {
                pick(agent);
            } else {
                GameScreen.toast("Requires: " + lock.getKey().getName());
            }
            return;
        }

        setOpened(!open, level);
    }

    private synchronized boolean setOpened(boolean opened, Level level) {
        if (activating || lock.isBroken()) {
            // cannot interrupt
            return false;
        }

        activating = true;
        open = opened;
        InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.DOOR_OPEN, getPosition());
        return true;
    }

    private void postActivation(Level level) {
        finished = false;

        for (Body body : bodies) {
            body.setActive(!open);
        }
        setLightWalls(level, !open);

        if (open) {
            onOpen();
        } else {
            onClose(getTriggerAgents(), level);
        }
    }

    private void onOpen() {
    }

    private void onClose(Iterable<Agent> triggerAgents, Level level) {
        for (Agent agent : triggerAgents) {
            // characters that lack this door's credentials trigger a lock in
            if (canTrap(agent) && !lock.canUnlock(agent)
                    && lock.getRoom().contains(agent.getNaturalPosition())
                    && lock.getRoom().hasHostileResident(agent)) {
                lock.getRoom().setLocked(true);
                for (Agent resident : lock.getRoom().getResidents()) {
                    if (!residents.contains(resident)) {
                        resident.addListener(this);
                        residents.add(resident);
                    }
                }
                break;
            }
        }
    }

    private boolean canTrap(Agent agent) {
        // for the purposes of immersion, we allow agents other than the main player to become
        // "trapped" within a room just as the player would, but in order to prevent blocking
        // the player's progress, only the player can be trapped in a room that lies on the
        // critical path
        return !lock.getRoom().onCriticalPath() || agent == agent.getLocation().getPlayer();
    }

    @Override
    public void onCellChange() {
    }

    @Override
    public void onDeath() {
        onResidentDeath();
    }

    private void onResidentDeath() {
        if (!hasHostileResident()) {
            lock.getRoom().setLocked(false);
        }
    }

    private boolean hasHostileResident() {
        for (Agent agent : getTriggerAgents()) {
            if (lock.getRoom().hasHostileResident(agent)) {
                return true;
            }
        }
        return false;
    }

    private void setColor() {
        if (lock.isLocked()) {
            light.setColor(1, 0, 0, 1f);
        } else {
            light.setColor(1, 1, 1, 1f);
        }
    }

    private void setLightWalls(Level level, boolean value) {
        Vector2 position = getRenderPosition();
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
    public void postRegister(Level level) {
        this.level = level;
        this.healthBar = level.createHealthBar();

        Vector2 position = getRenderPosition();
        float x = (int) position.x;
        float y = (int) position.y;

        Vector2 offset = new Vector2();
        if (front) {
            bodies.add(level.createEdge(x, y, x + SIZE, y));
            bodies.add(level.createEdge(x, y + 1, x + SIZE, y + 1));
            offset.set(1f, 0.5f);
        } else {
            x += 0.2f;
            y -= 1;
            bodies.add(level.createEdge(x + 0.2f, y, x + 0.2f, y + SIZE));
            bodies.add(level.createEdge(x + 0.5f, y, x + 0.5f, y + SIZE));
            offset.set(0.5f, 0f);
        }

        setLightWalls(level, true);
        level.addLight(light);

        // set the appropriate handler
        for (Body body : bodies) {
            for (Fixture fixture : body.getFixtureList()) {
                fixture.setUserData(bulletHandler);
            }
        }
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
        Vector2 position = getRenderPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();

        // render indicator
        super.render(delta, renderer);

        if (bulletHandler.isDamaged() && !lock.isBroken()) {
            // update and render health
            healthBar.update(this);
            healthBar.draw(level.getCamera());
        }
    }

    @Override
    public float getBaseHealth() {
        return bulletHandler.getBaseStrength();
    }

    @Override
    public float getHealth() {
        return bulletHandler.getStrength();
    }

    @Override
    public boolean isAlive() {
        return !lock.isBroken();
    }

    @Override
    public void setHealthIndicator(Vector3 worldCoords) {
        Vector2 position = getPosition();
        float h = getHeight() - getHeight() / 10f;
        worldCoords.set(position.x, position.y + h, 0);
    }

    @Override
    public float getZ() {
        if (open && !activating && front) {
            // always draw below everything else when open if we're a front door
            return Float.POSITIVE_INFINITY;
        }
        return getPosition().y;
    }

    public void lock(int strength) {
        lock.setStrength(strength);
        setLocked(true);
    }

    private void pick(Agent agent) {
        Icepik item = Icepik.from(agent.getLocation());
        int required = lock.getRequiredPiks(agent);
        agent.getInventory().removeItem(item, required);
        lock.getRoom().setLocked(false);
    }

    @Override
    public void crack(Agent source) {
        // unlock
        lock.getRoom().setLocked(false);
        // location.alertTo(agent);
    }

    public void destroy() {
        setLocked(false);
        setOpened(true, level);
        lock.breakLock();
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
        private final ConnectedRoom room;
        private int strength;
        private boolean locked;
        private boolean broken = false;

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
            locked = uniqueKey || shouldLock();
        }

        public ConnectedRoom getRoom() {
            return room;
        }

        public Item getKey() {
            return key.get();
        }

        public void setStrength(int strength) {
            this.strength = strength;
            if (shouldLock()) {
                locked = true;
            }
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

        private boolean shouldLock() {
            return strength > 1;
        }

        private boolean canPick(Agent agent) {
            int piks = getAvailablePiks(agent);
            int required = getRequiredPiks(agent);
            return piks >= required;
        }

        public int getAvailablePiks(Agent agent) {
            Icepik item = Icepik.from(agent.getLocation());
            if (agent.getInventory().hasItem(item)) {
                return agent.getInventory().getItemCount(item);
            }
            return 0;
        }

        public int getRequiredPiks(Agent agent) {
            int ability = agent.getInfo().getSubterfuge() / 10;
            int difficulty = strength;
            if (ability >= difficulty) {
                return 1;
            }

            // required piks scales with the square of the difference
            int delta = difficulty - ability;
            return delta * delta + 1;
        }

        public void breakLock() {
            broken = true;
        }

        public boolean isBroken() {
            return broken;
        }

        public static LockInfo from(ControlPoint controlPoint, ConnectedRoom room) {
            return new LockInfo(controlPoint.getRequiredKey(), controlPoint.getLockStrength(), room);
        }
    }

    private class DoorBulletHandler extends DamageHandler {
        private static final float BASE_HEALTH = 100f;
        private float health = BASE_HEALTH;

        public boolean isDamaged() {
            return getStrength() < getBaseStrength();
        }

        public float getBaseStrength() {
            return BASE_HEALTH;
        }

        public float getStrength() {
            return health;
        }

        @Override
        public boolean handle(Damager damager) {
            Damage damage = damager.getDamage();
            health -= damage.getDamageOf(DamageType.PHYSICAL)
                    + damage.getDamageOf(DamageType.THERMAL);
            if (health <= 0) {
                destroy();
            }
            return true;
        }
    }

    private static class DoorIndicator extends Indicator {
        private final WorldText lockText = new WorldText();
        private final LockInfo lock;

        public DoorIndicator(Texture texture, Vector2 renderOffset, LockInfo lock) {
            super(texture, renderOffset);
            this.lock = lock;
        }

        @Override
        protected void preRender(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
            if (!lock.canPick(getLevel().getPlayer())) {
                renderer.getBatch().setColor(Color.RED);
                lockText.setColor(Color.RED);
            } else {
                lockText.setColor(Color.WHITE);
            }
        }

        @Override
        protected void postRender(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
            // draw icepik requirements
            Level level = getLevel();
            float x = getX(owner);
            float y = getY(owner);
            float w = getWidth();
            float h = getHeight();
            lockText.render(String.valueOf(lock.getRequiredPiks(level.getPlayer())),
                    level.getCamera(), x + w, y + h);
        }

        @Override
        protected boolean isActive(Level level, ProximityActivator owner) {
            return !lock.isBroken() && lock.isLocked() && super.isActive(level, owner);
        }
    }
    
    private static Indicator createIndicator(LockInfo lock, boolean front) {
        Vector2 renderOffset = new Vector2();
        if (front) {
            renderOffset.set(1f, 1f);
        } else {
            renderOffset.set(0.5f, 0.5f);
        }
        return new DoorIndicator(PADLOCK, renderOffset, lock);
    }
}
