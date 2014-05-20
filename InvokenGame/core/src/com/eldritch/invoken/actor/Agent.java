package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.scifirpg.proto.Actors.ActorParams;

public abstract class Agent implements Entity {
    static AssetManager assetManager = new AssetManager();
    static float MAX_VELOCITY = 8f;
    static float JUMP_VELOCITY = 40f;
    static float DAMPING = 0.87f;
    public static int PX = 64;

    public enum Direction {
        Up, Left, Down, Right
    }

    enum State {
        Standing, Moving
    }

    public enum Activity {
        Explore, Combat, Cast, Thrust, Swipe, Death
    }

    final AgentInfo info;
    private final float width;
    private final float height;

    final Vector2 position = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Moving;
    Activity activity = Activity.Explore;
    Direction direction = Direction.Down;
    private final Map<Activity, Map<Direction, Animation>> animations;
    float stateTime = 0;

    private final LinkedList<Action> actions = new LinkedList<Action>();
    private final List<Effect> effects = new LinkedList<Effect>();
    private Action action = null;

    private Agent followed = null;
    private final List<Agent> followers = new ArrayList<Agent>();

    // hostilities: enemy -> provoked
    private final Map<Agent, Boolean> hostilities = new HashMap<Agent, Boolean>();
    private boolean confused = false;
    private int paralyzed = 0;

    private Agent target;
    private Npc interactor;
    private final Set<Class<?>> toggles = new HashSet<Class<?>>();

    public Agent(String assetPath, float x, float y, ActorParams params) {
        setPosition(x, y);
        animations = getAllAnimations(assetPath);

        // figure out the width and height of the player for collision
        // detection and rendering by converting a player frames pixel
        // size into world units (1 unit == 32 pixels)
        width = 1 / 32f * PX;
        height = 1 / 32f * PX;

        // health, level, augmentations, etc.
        info = new AgentInfo(this, params);
    }

    public Agent(String assetPath, float x, float y, Profession profession, int level) {
        setPosition(x, y);
        animations = getAllAnimations(assetPath);

        // figure out the width and height of the player for collision
        // detection and rendering by converting a player frames pixel
        // size into world units (1 unit == 32 pixels)
        width = 1 / 32f * PX; // regions[0][0].getRegionWidth();
        height = 1 / 32f * PX; // regions[0][0].getRegionHeight();

        // health, level, augmentations, etc.
        info = new AgentInfo(this, profession, level);
    }

    public float dst2(Agent other) {
        return position.dst2(other.position);
    }

    public Set<Faction> getFactions() {
        return info.factions.getFactions();
    }

    public void addFaction(Faction faction, int rank, int reputation) {
        info.factions.addFaction(faction, rank, reputation);
    }

    public int getReputation(Faction faction) {
        return info.factions.getReputation(faction);
    }

    public float getDisposition(Agent other) {
        return info.factions.getDisposition(other);
    }
    
    public PreparedAugmentations getAugmentations() {
        return info.augmentations;
    }

    public void useAugmentation(int index) {
        info.useAugmentation(index);
    }

    public void addAugmentation(Augmentation aug) {
        info.addAugmentation(aug);
    }

    public List<Agent> getFollowers() {
        return followers;
    }

    public boolean assaultedBy(Agent other) {
        return hostilities.containsKey(other) && hostilities.get(other);
    }

    public boolean hostileTo(Agent other) {
        return hostilities.containsKey(other);
    }

    public Iterable<Agent> getEnemies() {
        return hostilities.keySet();
    }

    public boolean hasEnemies() {
        return !hostilities.isEmpty();
    }

    public void addEnemy(Agent other, boolean attacked) {
        if (hostilities.containsKey(other)) {
            // we've already added them as an enemy, so don't do so again
            return;
        }

        if (attacked) {
            // we've been attacked
            // mark this hostility as provoked if we didn't assault them prior
            hostilities.put(other, !other.assaultedBy(this));
            other.addEnemy(this, false);
        } else {
            // we're attacking them
            // mark this hostility as unprovoked
            hostilities.put(other, false);
        }
    }

    public boolean isAlive() {
        return info.isAlive();
    }

    public float getHealth() {
        return info.getHealth();
    }

    public float damage(Agent source, float value) {
        if (isAlive()) {
            addEnemy(source, true);
        }
        return damage(value);
    }

    public float damage(float value) {
        return info.damage(value);
    }

    public float heal(float value) {
        return info.heal(value);
    }

    public void resurrect() {
        info.resetHealth();
    }

    public abstract void setConfused(boolean confused);

    public boolean isParalyzed() {
        return paralyzed > 0;
    }

    public void setParalyzed(Agent source, boolean paralyzed) {
        if (paralyzed) {
            addEnemy(source, true);
            this.paralyzed++;
        } else {
            this.paralyzed--;
        }
    }

    public void addFollower(Agent follower) {
        follower.setFollowing(this);
        followers.add(follower);
    }

    public void removeFollower(Agent follower) {
        follower.stopFollowing(this);
        followers.remove(follower);
    }

    public void setFollowing(Agent actor) {
        followed = actor;
    }

    public void stopFollowing(Agent actor) {
        // perform this check to avoid canceling the following of a different
        // actor
        if (followed == actor) {
            followed = null;
        }
    }

    public Agent getFollowed() {
        return followed;
    }

    public boolean isFollowing(Agent agent) {
        return getFollowed() == agent;
    }

    /** returns true if the toggle is on after invoking this method */
    public boolean toggle(Class<?> clazz) {
        if (toggles.contains(clazz)) {
            toggles.remove(clazz);
            return false;
        } else {
            toggles.add(clazz);
            return true;
        }
    }

    public boolean isToggled(Class<?> clazz) {
        return toggles.contains(clazz);
    }

    public boolean canAddAction() {
        // only add actions if there are none in the queue, meaning either none
        // or one is in
        // progress
        return actions.isEmpty();
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void addVelocity(float x, float y) {
        velocity.add(x, y);
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getMaxVelocity() {
        return MAX_VELOCITY;
    }

    public Direction getDirection() {
        return direction;
    }

    public Animation getAnimation(Direction direction) {
        return animations.get(activity).get(direction);
    }

    public Animation getAnimation(Activity activity) {
        return animations.get(activity).get(direction);
    }

    public void interact(Npc other) {
        interactor = other;
    }

    public Npc getInteractor() {
        return interactor;
    }

    public boolean isInteracting() {
        return interactor != null;
    }

    public boolean inDialogue() {
        return interactor != null && interactor.isAlive();
    }

    public boolean isLooting() {
        return interactor != null && !interactor.isAlive();
    }

    public void setTarget(Agent target) {
        this.target = target;
    }

    public Agent getTarget() {
        return target;
    }

    public boolean hasTarget() {
        return target != null;
    }

    public boolean canTarget(Location location) {
        return canTarget(target, location);
    }

    public boolean canTarget(Agent other, Location location) {
        // within distance constraint
        if (dst2(other) > 175) {
            return false;
        }
        return true;
    }

    public float getAttackScale(Agent other) {
        return info.getAccuracy() * getWeaponAccuracy() * (1.0f - other.getInfo().getDefense());
    }

    public float getExecuteScale(Agent other) {
        return info.getWillpower() * (1.0f - other.getInfo().getResistance());
    }

    public float getDeceiveScale(Agent other) {
        return info.getDeception() * (1.0f - other.getInfo().getPerception());
    }

    public boolean hasPendingAction() {
        return !actions.isEmpty();
    }

    protected void onDeath() {
        followers.clear();
        hostilities.clear();
        target = null;
    }

    protected void attemptTakeAction(float delta, Location screen) {
        if (isParalyzed()) {
            // can't do anything when paralyzed
            return;
        }

        if (confused) {
            // cannot take a conscious action when confused, must delegate to
            // the confusion handler
        } else {
            // no disorienting effects, so take conscious action
            takeAction(delta, screen);
        }
    }

    public void update(float delta, Location location) {
        if (delta == 0)
            return;
        stateTime += delta;

        // apply all active effects, remove any that are finished
        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect effect = it.next();
            if (!effect.isFinished()) {
                effect.apply(delta);
            } else {
                effect.dispel();
                it.remove();
            }
        }

        if (isAlive()) {
            // handle the action queue
            if (actionInProgress()) {
                action.update(delta);
            } else {
                action = actions.poll();
                if (action != null) {
                    action.apply();
                }
            }

            // take conscious action
            attemptTakeAction(delta, location);
        } else if (activity != Activity.Death) {
            // kill the agent
            onDeath();
        }

        // remove target if we're too far away from them
        if (hasTarget() && !canTarget(location)) {
            target = null;
        }

        // update followers
        Iterator<Agent> followerIterator = followers.iterator();
        while (followerIterator.hasNext()) {
            Agent follower = followerIterator.next();
            if (!follower.isAlive()) {
                followerIterator.remove();
            }
        }

        // update enemies
        Iterator<Agent> enemyIterator = hostilities.keySet().iterator();
        while (enemyIterator.hasNext()) {
            Agent enemy = enemyIterator.next();
            if (!enemy.isAlive()) {
                enemyIterator.remove();
            }
        }

        // set activity
        Activity last = activity;
        if (!isAlive()) {
            activity = Activity.Death;
        } else {
            activity = Activity.Explore;
        }

        // reset state if the activity was changed
        if (activity != last) {
            stateTime = 0;
        }

        if (actionInProgress()) {
            velocity.x = 0;
            velocity.y = 0;
        } else {
            move(delta, location);
        }
    }

    private void move(float delta, Location screen) {
        // clamp the velocity to the maximum
        if (Math.abs(velocity.x) > MAX_VELOCITY) {
            velocity.x = Math.signum(velocity.x) * MAX_VELOCITY;
        }

        if (Math.abs(velocity.y) > MAX_VELOCITY) {
            velocity.y = Math.signum(velocity.y) * MAX_VELOCITY;
        }

        // clamp the velocity to 0 if it's < 1, and set the state to
        // standing
        if (Math.abs(velocity.x) < 1 && Math.abs(velocity.y) < 1) {
            velocity.x = 0;
            velocity.y = 0;
            state = State.Standing;
        } else {
            if (target == null || target == this) {
                // update the current animation based on the maximal velocity
                // component
                direction = getDominantDirection(velocity.x, velocity.y);
            }
            state = State.Moving;
        }

        // do this separately so we can still get the standing state
        if (target != null && target != this) {
            float dx = target.position.x - position.x;
            float dy = target.position.y - position.y;
            direction = getDominantDirection(dx, dy);
        }

        // multiply by delta time so we know how far we go
        // in this frame
        velocity.scl(delta);

        // perform collision detection & response, on each axis, separately
        // if the actor is moving right, check the tiles to the right of
        // it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle actorRect = Location.getRectPool().obtain();
        getBoundingBox(actorRect);

        float relativeX = actorRect.x - actorRect.width / 2;
        float relativeY = actorRect.y - actorRect.height / 2;
        int startX, startY, endX, endY;

        // handle x-axis collisions
        startX = (int) (relativeX);
        endX = (int) (relativeX + getWidth());
        startY = (int) (relativeY);
        endY = (int) (relativeY + getHeight());
        screen.getTiles(startX, startY, endX, endY, screen.getTiles());

        Array<Agent> agents = getCollisionActors(screen);

        float oldX = actorRect.x;
        actorRect.x += velocity.x;
        if (collidesWith(actorRect, screen.getTiles())) {
            velocity.x = 0;
        }
        for (Agent agent : agents) {
            if (agent.collidesWith(actorRect)) {
                agent.addVelocity(velocity.x * 10, 0);
                velocity.x = 0;
                break;
            }
        }
        actorRect.x = oldX;

        // handle y-axis collisions
        startY = (int) (relativeY);
        endY = (int) (relativeY + getHeight());
        startX = (int) (relativeX);
        endX = (int) (relativeX + getWidth());
        screen.getTiles(startX, startY, endX, endY, screen.getTiles());

        float oldY = actorRect.y;
        actorRect.y += velocity.y;
        if (collidesWith(actorRect, screen.getTiles())) {
            velocity.y = 0;
        }
        for (Agent agent : agents) {
            if (agent.collidesWith(actorRect)) {
                agent.addVelocity(0, velocity.y * 10);
                velocity.y = 0;
                break;
            }
        }
        actorRect.y = oldY;

        Location.getRectPool().free(actorRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        position.add(velocity);
        velocity.scl(1 / delta);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        velocity.x *= DAMPING;
        velocity.y *= DAMPING;
    }

    private Array<Agent> getCollisionActors(Location screen) {
        Array<Agent> agents = new Array<Agent>();
        for (Agent other : screen.getActors()) {
            // only collide with enemies
            if (other == this)
                continue;

            // avoid sqrt because it is relatively expensive and unnecessary
            float a = position.x - other.position.x;
            float b = position.y - other.position.y;
            float distance = a * a + b * b;

            // our tolerance is the combined radii of both actors
            float w = getWidth() / 2 + other.getWidth() / 2;
            float h = getHeight() / 2 + other.getHeight() / 2;
            float tol = w * w + h * h;

            if (distance <= tol) {
                agents.add(other);
            }
        }
        return agents;
    }

    private boolean collidesWith(Rectangle actorRect) {
        Rectangle rect = getBoundingBox(Location.getRectPool().obtain());
        boolean result = actorRect.overlaps(rect);
        Location.getRectPool().free(rect);
        return result;
    }

    private boolean collidesWith(Rectangle actorRect, Array<Rectangle> rects) {
        for (Rectangle tile : rects) {
            if (actorRect.overlaps(tile)) {
                return true;
            }
        }
        return false;
    }

    private boolean actionInProgress() {
        return action != null && !action.isFinished();
    }

    public float getX1() {
        return position.x - getWidth() / 2;
    }

    public float getY1() {
        return position.y - getWidth() / 2;
    }

    protected Vector2 getForwardVector() {
        Vector2 result = new Vector2();
        switch (direction) {
            case Left:
                result.set(-1, 0);
                break;
            case Right:
                result.set(1, 0);
                break;
            case Down:
                result.set(0, -1);
                break;
            case Up:
                result.set(0, 1);
                break;
        }
        return result;
    }

    private Direction getDominantDirection(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            if (x < 0) {
                // left
                return Direction.Left;
            } else {
                // right
                return Direction.Right;
            }
        } else {
            if (y < 0) {
                // down
                return Direction.Down;
            } else {
                // up
                return Direction.Up;
            }
        }
    }

    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        render(renderer);

        // render all unfinished effects
        for (Effect effect : effects) {
            if (!effect.isFinished()) {
                effect.render(delta, renderer);
            }
        }
    }

    public void render(OrthogonalTiledMapRenderer renderer) {
        Activity activity = this.activity;
        float stateTime = this.stateTime;
        if (isAlive() && actionInProgress()) {
            stateTime = action.getStateTime();
            activity = action.getActivity();
        } else if (state == State.Standing && activity == Activity.Explore) {
            stateTime = 0;
        }

        // render equipment
        if (info.getInventory().hasOutfit()) {
            Outfit outfit = info.getInventory().getOutfit();
            if (!outfit.covers()) {
                // draw the actor, depending on the current velocity
                // on the x-axis, draw the actor facing either right
                // or left
                render(activity, direction, stateTime, renderer);
            }
            outfit.render(this, activity, stateTime, renderer);
        } else {
            render(activity, direction, stateTime, renderer);
        }

        // render action effects
        if (actionInProgress() && isAlive()) {
            action.render(renderer);
        }
    }

    private void render(Activity activity, Direction direction, float stateTime,
            OrthogonalTiledMapRenderer renderer) {
        // based on the actor state, get the animation frame
        TextureRegion frame = animations.get(activity).get(direction).getKeyFrame(stateTime);
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(frame, position.x - getWidth() / 2, position.y - getHeight() / 2, getWidth(),
                getHeight());
        batch.end();
    }

    public Rectangle getBoundingBox(Rectangle rect) {
        rect.set(position.x - getWidth() / 4, position.y - getHeight() / 2, getWidth() / 2,
                getHeight() / 4);
        return rect;
    }

    public boolean contains(float x, float y) {
        return x >= position.x - getWidth() / 2 && x <= position.x + getWidth() / 2
                && y >= position.y - getHeight() / 2 && y <= position.y + getHeight() / 2;
    }

    protected void setState(State state) {
        this.state = state;
    }

    public float getWeaponAccuracy() {
        // TODO separate weapon class
        return 0.65f;
    }

    public boolean hasWeapon() {
        return info.getInventory().hasWeapon();
    }

    public RangedWeapon getWeapon() {
        return info.getInventory().getWeapon();
    }

    public AgentInfo getInfo() {
        return info;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    protected abstract void takeAction(float delta, Location screen);

    protected abstract void handleInteract(Agent agent);

    public static Animation getAnimation(String assetName) {
        TextureRegion[][] regions = GameScreen.getRegions(assetName, PX, PX);
        Animation anim = new Animation(0.15f, regions[0]);
        return anim;
    }

    public static Map<Direction, Animation> getAnimations(String assetName) {
        Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        // up, left, down, right
        TextureRegion[][] regions = GameScreen.getRegions(assetName, PX, PX);
        for (Direction d : Direction.values()) {
            Animation anim = new Animation(0.15f, regions[d.ordinal()]);
            anim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            animations.put(d, anim);
        }

        return animations;
    }

    public static Map<Activity, Map<Direction, Animation>> getAllAnimations(String assetName) {
        Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();
        TextureRegion[][] regions = GameScreen.getRegions(assetName, PX, PX);

        // cast
        int offset = 0;
        animations.put(Activity.Cast, getAnimations(regions, 7, offset));

        // thrust
        offset += Direction.values().length;
        animations.put(Activity.Thrust, getAnimations(regions, 8, offset));

        // walk
        offset += Direction.values().length;
        animations.put(Activity.Explore, getAnimations(regions, 9, offset));

        // swipe
        offset += Direction.values().length;
        animations.put(Activity.Swipe, getAnimations(regions, 6, offset));

        // shoot
        offset += Direction.values().length;
        animations.put(Activity.Combat, getAnimations(regions, 13, offset));

        // hurt
        offset += Direction.values().length;
        animations.put(Activity.Death,
                getAnimations(regions, 6, offset, false, Animation.PlayMode.NORMAL));

        return animations;
    }

    private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int length,
            int offset) {
        return getAnimations(regions, length, offset, true, Animation.PlayMode.LOOP);
    }

    private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int length,
            int offset, boolean increment, Animation.PlayMode playMode) {
        int index = offset;
        Map<Direction, Animation> directions = new HashMap<Direction, Animation>();
        for (Direction d : Direction.values()) {
            TextureRegion[] textures = Arrays.copyOfRange(regions[index], 0, length);
            Animation anim = new Animation(0.15f, textures);
            anim.setPlayMode(playMode);
            directions.put(d, anim);
            if (increment) {
                index++;
            }
        }
        return directions;
    }
}