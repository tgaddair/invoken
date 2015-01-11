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

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.GameCamera;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Cloak;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.type.HandledProjectile.ProjectileHandler;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.ui.MultiTextureRegionDrawable;

public abstract class Agent extends CollisionEntity implements Steerable<Vector2> {
    public static final int MAX_DST2 = 175;
    public static final int ASSAULT_PENALTY = -10;

    static AssetManager assetManager = new AssetManager();
    static float MAX_FREEZE = 25f;
    static float DAMPING = 5f;
    
    private final World world;
    protected final Body body;
    private final float radius;

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
    State state = State.Moving;
    Activity activity = Activity.Explore;
    Direction direction = Direction.Down;
    private final Map<Activity, Map<Direction, Animation>> animations;
    float stateTime = 0;
    float elapsed = 0;

    private final List<Agent> neighbors = new ArrayList<Agent>();
    private final LinkedList<Action> actions = new LinkedList<Action>();
    private final List<Effect> effects = new LinkedList<Effect>();
    private Action action = null;

    private Agent followed = null;
    private final List<Agent> followers = new ArrayList<Agent>();

    // hostilities: agents with negative reaction who have attacked us
    private final Set<Agent> assaulters = new HashSet<Agent>();  // assaulters attack those who have no enemies
    private final Set<Agent> enemies = new HashSet<Agent>();  // convenience collection for fast lookups
    private final Map<Agent, Float> relations = new HashMap<Agent, Float>();

    private int confused = 0;
    private int paralyzed = 0;
    private float freezing = 0;
    private float lastAction = 0;

    private Agent target;
    private Npc interactor;
    private final Set<Class<?>> toggles = new HashSet<Class<?>>();
    private final Set<ProjectileHandler> projectileHandlers = new HashSet<ProjectileHandler>();
    private final LineOfSightHandler losHandler = new LineOfSightHandler();

    private final Color color = new Color(1, 1, 1, 1);

    public Agent(ActorParams params, float x, float y, float width, float height,
            World world, Map<Activity, Map<Direction, Animation>> animations) {
        super(width, height);
        setPosition(x, y);
        this.animations = animations;

        // health, level, augmentations, etc.
        info = new AgentInfo(this, params);
        radius = Math.max(width, height) / 5;
        this.world = world;
        body = createBody(x, y, width, height, world);
    }

    public Agent(float x, float y, float width, float height, Profession profession, int level,
            World world, Map<Activity, Map<Direction, Animation>> animations) {
        super(width, height);
        setPosition(x, y);
        this.animations = animations;

        // health, level, augmentations, etc.
        info = new AgentInfo(this, profession, level);
        radius = Math.max(width, height) / 5;
        this.world = world;
        body = createBody(x, y, width, height, world);
    }
    
	private Body createBody(float x, float y, float width, float height, World world) {
		CircleShape circleShape = new CircleShape();
		circleShape.setPosition(new Vector2());
		circleShape.setRadius(radius);

		BodyDef characterBodyDef = new BodyDef();
		characterBodyDef.position.set(x, y);
		characterBodyDef.type = BodyType.DynamicBody;
		Body body = world.createBody(characterBodyDef);

		FixtureDef charFixtureDef = new FixtureDef();
		charFixtureDef.density = getDensity();
		charFixtureDef.shape = circleShape;
		charFixtureDef.filter.groupIndex = 0;
		body.createFixture(charFixtureDef);
		
		body.setLinearDamping(DAMPING);
		body.setAngularDamping(10);

		circleShape.dispose();
		return body;
	}
	
	public float getDensity() {
		return 1;
	}

    public void setRgb(float r, float g, float b) {
        color.set(r, g, b, color.a);
    }

    public void setAlpha(float a) {
        color.set(color.r, color.g, color.b, a);
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
    
    public boolean inRange(Vector2 point, float radius) {
    	return body.getPosition().dst2(point) <= radius * radius;
    }
    
	public List<Agent> getNeighbors() {
	    return neighbors;
	}

    public List<Agent> getFollowers() {
        return followers;
    }

    public boolean isAlive() {
        return info.isAlive();
    }

    public float getHealth() {
        return info.getHealth();
    }

    public float damage(Agent source, float value) {
        if (isAlive()) {
            addHostility(source, value);
        }
        return damage(value);
    }

    public float damage(float value) {
        setCloaked(false); // damage breaks cloaking
        return info.damage(value);
    }

    public float heal(float value) {
        return info.heal(value);
    }

    public void resurrect() {
        info.resetHealth();
        setRgb(0.4f, 0.4f, 0.7f);
    }
    
    public void setCamera(GameCamera camera) {
    }
    
    public void resetCamera() {
    }
    
    public boolean usingRemoteCamera() {
    	return false;
    }

    public float getVisibility() {
        return color.a;
    }

    public boolean isConfused() {
        return confused > 0;
    }
    
    public void addProjectileHandler(ProjectileHandler handler) {
        projectileHandlers.add(handler);
    }
    
    public void removeProjectileHandler(ProjectileHandler handler) {
        projectileHandlers.remove(handler);
    }
    
    public void handleProjectile(HandledProjectile handledProjectile) {
        boolean handled = false;
        for (ProjectileHandler handler : projectileHandlers) {
            boolean result = handler.handle(handledProjectile);
            if (result) {
                handled = true;
                break;
            }
        }
        
        if (!handled) {
            handledProjectile.apply(this);
        }
    }

    public void setConfused(boolean confused) {
        this.confused += confused ? 1 : -1;
        if (confused || this.confused == 0) {
            handleConfusion(confused);
            if (confused) {
                setRgb(1, 0, 0);
            } else {
                setRgb(1, 1, 1);
            }
        }
    }
    
    public void freeze(float magnitude) {
    	// apply freezing effects
    	freezing += magnitude;
    	if (freezing > 0) {
    		float f = Math.max((MAX_FREEZE - freezing) / MAX_FREEZE, 0f);
    		setDamping(DAMPING * f);
    		setRgb(0.69f, 0.88f, 0.90f);
    	} else {
    		setDamping(DAMPING);
    		setRgb(1, 1, 1);
    	}
    }
    
    public float getFreezing() {
    	return freezing;
    }
    
    public boolean isFrozen(float lastAction) {
    	float percent = Math.min(freezing / MAX_FREEZE, 1f);
    	return lastAction < percent;
    }
    
    public boolean isFrozen() {
    	return freezing / MAX_FREEZE >= 1;
    }
    
    public void setDamping(float damping) {
    	body.setLinearDamping(damping);
    }

    protected abstract void handleConfusion(boolean confused);

    public boolean isParalyzed() {
        return paralyzed > 0;
    }

    public void setParalyzed(Agent source, boolean paralyzed) {
        if (paralyzed) {
            addHostility(source, 10);
        }
        setParalyzed(paralyzed);
    }
    
    public void setParalyzed(boolean paralyzed) {
    	if (paralyzed) {
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
    
    public boolean isFollowing() {
    	return followed != null;
    }

    public boolean isFollowing(Agent agent) {
        return getFollowed() == agent;
    }

    public boolean isCloaked() {
        return toggles.contains(Cloak.class);
    }

    public void setCloaked(boolean cloaked) {
        if (cloaked) {
            toggles.add(Cloak.class);
            setAlpha(0.1f);
        } else {
            toggles.remove(Cloak.class);
            setAlpha(1);
        }
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
    
    public void toggleOn(Class<?> clazz) {
        toggles.add(clazz);
    }
    
    public void toggleOff(Class<?> clazz) {
        toggles.remove(clazz);
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
        // or one is in progress
        return actions.isEmpty();
    }

    public void addAction(Action action) {
        actions.add(action);
        info.expend(action.getCost());
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
    
    public Vector2 getVisibleCenter() {
    	return position.cpy().add(getWidth() / 2, 0);
    }
    
    public Vector2 getRenderPosition() {
    	return position;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    public NaturalVector2 getCellPosition() {
        return NaturalVector2.of((int) position.x, (int) position.y);
    }
    
    public void applyForce(Vector2 force) {
    	body.applyForceToCenter(force, true);
    }
    
    public void stop() {
    	body.setLinearVelocity(0, 0);
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public void setDirection(Direction direction) {
    	this.direction = direction;
    }
    
    public Direction getRelativeDirection(Vector2 point) {
    	return getDominantDirection(point.x - position.x, point.y - position.y);
    }

    public Direction getDirection() {
        return direction;
    }

    public TextureRegionDrawable getPortrait() {
        TextureRegion region = animations.get(Activity.Explore).get(Direction.Right).getKeyFrame(0);
        if (info.getInventory().hasOutfit()) {
            Outfit outfit = info.getInventory().getOutfit();
            TextureRegion outfitRegion = outfit.getPortrait();
            if (outfit.covers()) {
                return new MultiTextureRegionDrawable(outfitRegion);
            } else {
                return new MultiTextureRegionDrawable(region, outfitRegion);
            }
        }
        return new MultiTextureRegionDrawable(region);
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
        if (!isNear(other)) {
            // not within distance constraint
            return false;
        }
        if (!isVisible(other)) {
            // cannot see visibly
            return false;
        }
        return true;
    }

    public boolean isVisible(Agent other) {
        return other.getVisibility() >= Math.min(10f / info.getSubterfuge(), 1.0f);
    }

    public boolean isNear(Agent other) {
        return dst2(other) <= MAX_DST2;
    }
    
    public boolean canTargetProjectile(Agent other) {
    	return hasLineOfSight(other);
    }
    
    public boolean hasLineOfSight(Agent other) {
    	losHandler.reset(other);
        world.rayCast(losHandler, position, other.position);
        return losHandler.hasLineOfSight();
    }
    
    public boolean hasLineOfSight(Vector2 target) {
    	losHandler.reset(null);
        world.rayCast(losHandler, position, target);
        return losHandler.hasLineOfSight();
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
        enemies.clear();
        actions.clear();
        action = null;
        target = null;
        toggles.clear();
        setRgb(1, 1, 1);
    }

    protected void attemptTakeAction(float delta, Location location) {
    	lastAction += delta;
        if (isParalyzed()) {
            // can't do anything when paralyzed
            return;
        }
        
        if (isFrozen(lastAction)) {
        	// can't act as frequently when we're frozen
        	return;
        }

        if (isConfused()) {
            // cannot take a conscious action when confused, must delegate to
            // the confusion handler
        } else {
        	// update neighbors
        	location.getNeighbors(this);
        	
            // no disorienting effects, so take conscious action
        	lastAction = 0;
            takeAction(delta, location);
        }
    }
    
    public boolean assaultedBy(Agent other) {
        return assaulters.contains(other);
    }
    
    public boolean hostileTo(Agent other) {
        return enemies.contains(other);
    }

    public Iterable<Agent> getEnemies() {
        return enemies;
    }

    public boolean hasEnemies() {
        return !enemies.isEmpty();
    }
    
    public void addHostility(Agent source, float magnitude) {
    	if (!hasEnemies()) {
    		// we're not in combat with anyone, so this is considered assault
    		assaulters.add(source);
    	}
    	
    	changeRelation(source, -magnitude);
    }
    
    public float changeRelation(Agent target, float delta) {
        info.changePersonalRelation(target, delta);
        updateDisposition(target);
        return getRelation(target);
    }
    
    public float changeFactionRelations(Agent target, float delta) {
        target.info.getFactionManager().modifyReputationFor(this, delta);
        return getRelation(target);
    }
    
    public float getRelation(Agent agent) {
        if (!relations.containsKey(agent)) {
            setRelation(agent, info.getDisposition(agent));
        }
        return relations.get(agent);
    }
    
    public void changeFactionStatus(Faction faction, float delta) {
        info.getFactionManager().modifyReputationFor(faction, delta);
    }
    
    public void updateDisposition(Agent agent) {
        if (relations.containsKey(agent)) {
            // only update the disposition if it was previously calculated
        	float relation = info.getDisposition(agent);
        	setRelation(agent, relation);
        }
    }
    
    private void setRelation(Agent agent, float relation) {
    	relations.put(agent, relation);
        updateReaction(agent, relation);
    }
    
    private void updateReaction(Agent agent, float relation) {
    	if (!enemies.contains(agent) && Behavior.isEnemyGiven(relation)) {
            // unfriendly, so mark them as an enemy
            enemies.add(agent);
            
            if (assaultedBy(agent)) {
                // they attacked us, mark them as an assaulter
                // assault carriers a severe penalty for ranking factions
                changeFactionRelations(agent, ASSAULT_PENALTY);
        	}
    	}
    }
    
    @Override
    public void update(float delta, Location location) {
        if (delta == 0)
            return;
        stateTime += delta;
        
        elapsed += delta;
        if (elapsed >= 3) {
            // restore 1 unit of energy every 3 seconds
            info.restore(1);
            elapsed = 0;
        }
        
        updateHeading();

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
            // paralysis prevents actions from continuing
            if (!isParalyzed()) {
                // handle the action queue
                if (actionInProgress()) {
                    action.update(delta, location);
                } else {
                    action = actions.poll();
                    if (action != null) {
                        // any action breaks cloaking
                        setCloaked(false);
                        action.update(delta, location);
                    }
                }
    
                // take conscious action
                attemptTakeAction(delta, location);
            }
        } else if (activity != Activity.Death) {
            // kill the agent
            onDeath();
        }

        // remove target if we can no longer target them
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
        Iterator<Agent> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Agent enemy = enemyIterator.next();
            if (!enemy.isAlive() || !canTarget(enemy, location)) {
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

        // clamp the velocity to 0 if it's < 1, and set the state to
        // standing
        if ((Math.abs(velocity.x) < .01 && Math.abs(velocity.y) < .01) || isParalyzed()) {
            state = State.Standing;
        } else if (Math.abs(velocity.x) > .01 || Math.abs(velocity.y) > .01) {
        	// only update direction if we are going pretty fast
            if (target == null || target == this) {
                // update the current animation based on the maximal velocity
                // component
            	if (!actionInProgress()) {
            		// don't update the direction if we're currently performing an action
            		direction = getDominantDirection(velocity.x, velocity.y);
            	}
            }
            state = State.Moving;
        }

        // do this separately so we can still get the standing state
        if (target != null && target != this) {
            float dx = target.position.x - position.x;
            float dy = target.position.y - position.y;
            direction = getDominantDirection(dx, dy);
        }
        
        position.set(body.getPosition().cpy().add(0, getHeight() / 2 - radius));
		velocity.set(body.getLinearVelocity());
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

    public boolean collidesWith(Rectangle actorRect) {
        Rectangle rect = getBoundingBox(Location.getRectPool().obtain());
        boolean result = actorRect.overlaps(rect);
        Location.getRectPool().free(rect);
        return result;
    }

    protected boolean collidesWith(Rectangle actorRect, Array<Rectangle> rects) {
        for (Rectangle tile : rects) {
            if (actorRect.overlaps(tile)) {
                return true;
            }
        }
        return false;
    }

    public boolean actionInProgress() {
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
    
    public Vector2 getBackwardVector() {
        // used for wall normals
        return getForwardVector().scl(-1);
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

        // apply color
        Batch batch = renderer.getSpriteBatch();
        batch.setColor(color);

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

        // restore original color
        batch.setColor(Color.WHITE);

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
        float w = getWidth() / 3f;
        rect.set(position.x - w / 2, position.y - getHeight() / 2, w, getHeight() / 4);
        return rect;
    }
    
	public float getBoundingRadius() {
		return getWidth() / 2f;
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
	
	private class LineOfSightHandler implements RayCastCallback {
		private boolean lineOfSight = true;
		private Agent target = null;
		
		public boolean hasLineOfSight() {
			return lineOfSight;
		}
		
		public void reset(Agent agent) {
			lineOfSight = true;
			target = agent;
		}
		
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			if (!targetFixture(fixture)) {
				lineOfSight = false;
			}
			return fraction;
		}
		
		private boolean targetFixture(Fixture fixture) {
			for (Fixture f : body.getFixtureList()) {
				if (fixture == f) {
					return true;
				}
			}
			
			if (target == null) {
				return false;
			}
			
			for (Fixture f : target.body.getFixtureList()) {
				if (fixture == f) {
					return true;
				}
			}
			return false;
		}
	};
}