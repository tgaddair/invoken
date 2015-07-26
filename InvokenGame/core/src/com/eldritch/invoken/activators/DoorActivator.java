package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.util.LockManager;
import com.eldritch.invoken.activators.util.LockManager.LockCallback;
import com.eldritch.invoken.activators.util.LockManager.LockIndicator;
import com.eldritch.invoken.activators.util.LockManager.LockInfo;
import com.eldritch.invoken.actor.items.Icepik;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.AgentListener;
import com.eldritch.invoken.actor.util.Damageable;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.Constants;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

public class DoorActivator extends ClickActivator implements Crackable, Damageable, AgentListener,
        LockCallback {
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

    private final LockManager lockManager;
    private final LockInfo lock;
    private final Animation unlockedAnimation;
    private final Animation lockedAnimation;

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
        return new DoorActivator(x, y, 2, 2, center, lock, true);
    }

    public static DoorActivator createSide(int x, int y, LockInfo lock) {
        Vector2 center = new Vector2(x + 0.5f, y);
        return new DoorActivator(x, y, 1, 3, center, lock, false);
    }

    public DoorActivator(int x, int y, int width, int height, Vector2 center, LockInfo lock,
            boolean front) {
        super(x, y, width, height, ProximityParams.of(center).withIndicator(
                createIndicator(lock, front)));
        this.lockManager = new LockManager(lock, this);
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

        lock.getRoom().addDoor(this);
    }

    @Override
    public void preUpdate(float delta, Level level) {
        // only change the state of the door if it differs from the current
        // state must click to unlock
        if (!activating) {
            if (!open && lockChange.isPresent()) {
                applyLocked(lockChange.get());
            }
        }

        if (finished) {
            postActivation(level);
        }
    }

    @Override
    protected boolean shouldActivate(boolean hasProximity) {
        if (!super.shouldActivate(hasProximity)) {
            return false;
        }

        if (!lock.isLocked()) {
            return true;
        }

        if (!hasProximity || hasCredentials()) {
            return true;
        }

        return false;
    }

    private boolean hasCredentials() {
        for (Agent agent : getProximityAgents()) {
            if (lock.canUnlock(agent)
                    || agent.getInfo().hasRank(
                            agent.getLocation().getFaction(Constants.STATION_FACTION))) {
                // members of this faction can open all doors
                return true;
            }
        }
        return false;
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

        if (open != opened) {
            // set animations
            stateTime = 0;
            PlayMode mode = opened ? PlayMode.NORMAL : PlayMode.REVERSED;
            lockedAnimation.setPlayMode(mode);
            unlockedAnimation.setPlayMode(mode);
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.DOOR_OPEN, getPosition());
        }

        activating = true;
        open = opened;

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
            bodies.add(level.createEdge(x + 0.2f, y, x + 0.2f, y + SIZE + 1));
            bodies.add(level.createEdge(x + 0.5f, y, x + 0.5f, y + SIZE + 1));
            offset.set(0.5f, 0f);
        }

        setLightWalls(level, true);
        level.addLight(light);

        // set the appropriate handler
        lockManager.register(bodies);
    }

    @Override
    public void preRender(float delta, OrthogonalTiledMapRenderer renderer) {
        Animation animation = lock.isLocked() ? lockedAnimation : unlockedAnimation;
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                finished = true;
                activating = false;
            }
        }

        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getRenderPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, SIZE, getHeight());
        batch.end();

        if (lockManager.isDamaged() && !lock.isBroken()) {
            // update and render health
            healthBar.update(this);
            healthBar.draw(level.getCamera());
        }
    }

    @Override
    protected void postRender(float delta, OrthogonalTiledMapRenderer renderer) {
        // do this to draw the indicator, which is otherwise hidden for some reason
        // TODO: figure out why
        renderOverlay(delta, renderer);
    }

    @Override
    public float getBaseHealth() {
        return lockManager.getBaseHealth();
    }

    @Override
    public float getHealth() {
        return lockManager.getHealth();
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

    @Override
    public boolean inOverlay() {
        return !front;
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

    @Override
    public void destroyBy(Agent source) {
        setLocked(false);
        setOpened(true, level);
        lock.breakLock();

        // notify of vandalism
        level.getCrimeManager().commitVandalism(source, lock.getRoom());
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

    private static Indicator createIndicator(LockInfo lock, boolean front) {
        Vector2 renderOffset = new Vector2();
        if (front) {
            renderOffset.set(1f, 1f);
        } else {
            renderOffset.set(0.5f, 1f);
        }
        return new LockIndicator(PADLOCK, renderOffset, lock);
    }
}
