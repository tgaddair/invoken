package com.eldritch.invoken.actor.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.scifirpg.proto.Actors.ActorParams;

public abstract class Agent extends CollisionEntity {
    public static final int MAX_DST2 = 175;
    
    static AssetManager assetManager = new AssetManager();
    static float DAMPING = 0.87f;

    public enum Direction {
        Up, Left, Down, Right
    }

    enum State {
        Standing, Moving
    }

    public enum Activity {
        Explore, Combat, Cast, Thrust, Swipe, Death
    }
    
    public enum Hostility {
        Defensive, Assault
    }

    final AgentInfo info;
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

    // hostilities: agents with negative reaction who have attacked us
    Hostility hostility = Hostility.Defensive;
    private final Set<Agent> hostilities = new HashSet<Agent>();
    private final Set<Agent> assaulters = new HashSet<Agent>();
    private final Map<Agent, Float> relations = new HashMap<Agent, Float>();
    
    private boolean confused = false;
    private int paralyzed = 0;

    private Agent target;
    private Npc interactor;
    private final Set<Class<?>> toggles = new HashSet<Class<?>>();

    public Agent(ActorParams params, float x, float y, float width, float height, 
            Map<Activity, Map<Direction, Animation>> animations) {
        super(width, height);
        setPosition(x, y);
        this.animations = animations;

        // health, level, augmentations, etc.
        info = new AgentInfo(this, params);
    }

    public Agent(float x, float y, float width, float height, Profession profession, int level,
            Map<Activity, Map<Direction, Animation>> animations) {
        super(width, height);
        setPosition(x, y);
        this.animations = animations;

        // health, level, augmentations, etc.
        info = new AgentInfo(this, profession, level);
    }

    public float dst2(Agent other) {
        return position.dst2(other.position);
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
        return assaulters.contains(other);
    }

    public boolean hostileTo(Agent other) {
        return hostilities.contains(other);
    }

    public Iterable<Agent> getEnemies() {
        return hostilities;
    }

    public boolean hasEnemies() {
        return !hostilities.isEmpty();
    }

    public void addEnemy(Agent other, float magnitude) {
        if (other.assaultedBy(this)) {
            // we previously assaulted them, so add them as an enemy, but don't adjust our relation
            hostilities.add(other);
        } else {
            float modifier = -magnitude;
            if (!hostilities.isEmpty()) {
                // friendly fire is inevitable once bullets start flying, so relax the penalty
                modifier *= 0.1f;
            }
            
            // lower our disposition
//            System.out.println(String.format("relation (%s -> %s) : %f, mod=%f", 
//                    getInfo().getName(), other.getInfo().getName(), getRelation(other), modifier));
            float relation = changeRelation(other, modifier);
            if (Behavior.isEnemyGiven(relation)) {
                // unfriendly, so mark them as an enemy
                hostilities.add(other);
                if (hostility == Hostility.Defensive) {
                    // they attacked us, mark them as an assaulter
                    other.hostility = Hostility.Assault;
                    assaulters.add(other);
                    
                    // lower their reputation with all our factions
                }
            }
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
            addEnemy(source, value);
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
            addEnemy(source, 10);
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
    
    public Iterable<Action> getReverseActions() {
        // start just after the last element of the list
        final ListIterator<Action> it = actions.listIterator(actions.size());
        final Iterator<Action> reverse = new Iterator<Action>() {
            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public Action next() {
                return it.previous();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove from reverse iterator!");
            }
        };
        return new Iterable<Action>() {
            @Override
            public Iterator<Action> iterator() {
                return reverse;
            }
        };
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
    
    public void removeAction() {
        if (!actions.isEmpty()) {
            // remove the most recently added action from the back of the queue
            actions.removeLast();
        }
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
    
    public NaturalVector2 getCellPosition() {
        return NaturalVector2.of((int) position.x, (int) position.y);
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
        if (dst2(other) > MAX_DST2) {
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
        relations.clear();
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
    
    public Map<Agent, Float> getRelations() {
        return relations;
    }
    
    public float getRelation(Agent agent) {
        if (!relations.containsKey(agent)) {
            relations.put(agent, info.getDisposition(agent));
        }
        return relations.get(agent);
    }
    
    public float changeRelation(Agent agent, float delta) {
        float relation = getRelation(agent) + delta;
        relations.put(agent, relation);
        return relation;
    }

    @Override
    public void update(float delta, Location location) {
        if (delta == 0)
            return;
        stateTime += delta;
        
        // cleanup relations
        {
            Iterator<Entry<Agent, Float>> it = relations.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Agent, Float> entry = it.next();
                if (!entry.getKey().isAlive()) {
                    it.remove();
                }
            }
        }

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
                action.update(delta, location);
            } else {
                action = actions.poll();
                if (action != null) {
                    action.update(delta, location);
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
        Iterator<Agent> enemyIterator = hostilities.iterator();
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
        if (Math.abs(velocity.x) > getMaxVelocity()) {
            velocity.x = Math.signum(velocity.x) * getMaxVelocity();
        }

        if (Math.abs(velocity.y) > getMaxVelocity()) {
            velocity.y = Math.signum(velocity.y) * getMaxVelocity();
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
    
    public boolean collidesWith(float x, float y) {
        if (!isAlive()) {
            // cannot collide with a point if not alive
            return false;
        }
        
        Rectangle rect = getLargeBoundingBox(Location.getRectPool().obtain());
        boolean result = rect.contains(x, y);
        Location.getRectPool().free(rect);
        return result;
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

    public Vector2 getForwardVector() {
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

    @Override
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
        float width = 1 / 32f * frame.getRegionWidth();
        float height = 1 / 32f * frame.getRegionHeight();
        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
        batch.end();
    }
    
    public Rectangle getLargeBoundingBox(Rectangle rect) {
        rect.set(position.x - getWidth() / 4, position.y - getHeight() / 2, getWidth() / 2,
                getHeight());
        return rect;
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
    
    public Inventory getInventory() {
        return info.getInventory();
    }

    public AgentInfo getInfo() {
        return info;
    }
    
    @Override
    public String toString() {
        return info.getName();
    }
    
    public abstract float getMaxVelocity();

    protected abstract void takeAction(float delta, Location screen);

    protected abstract void handleInteract(Agent agent);
}