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
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.Conversable;
import com.eldritch.invoken.actor.GameCamera;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Cloak;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.HandledProjectile.ProjectileHandler;
import com.eldritch.invoken.actor.util.ActivationHandler;
import com.eldritch.invoken.actor.util.Announcement;
import com.eldritch.invoken.actor.util.Announcement.BanterAnnouncement;
import com.eldritch.invoken.actor.util.Announcement.BasicAnnouncement;
import com.eldritch.invoken.actor.util.Announcement.ResponseAnnouncement;
import com.eldritch.invoken.actor.util.Interactable;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.actor.util.Lootable;
import com.eldritch.invoken.actor.util.MotionTracker;
import com.eldritch.invoken.actor.util.ThreatMonitor;
import com.eldritch.invoken.effects.Effect;
import com.eldritch.invoken.effects.HoldingWeapon;
import com.eldritch.invoken.effects.Sprinting;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.ui.MultiTextureRegionDrawable;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class Agent extends CollisionEntity implements Steerable<Vector2>, Conversable,
        Lootable, Interactable {
    public static final int MAX_DST2 = 150;
    public static final int INTERACT_RANGE = 5;
    public static final float UNMASK_RANGE = 10;
    public static final int ASSAULT_PENALTY = -25;
    public static final float AIMING_V_PENALTY = 5;

    public static final float DODGE_SCALE = 500f;
    public static final float SPRINT_SCALE = 0.75f;
    public static final float DODGE_COST = 10f;
    public static final float SPRINT_COST = 5f;

    private static final float ROTATION_SCALE = 4.5f;

    static AssetManager assetManager = new AssetManager();
    static float MAX_FREEZE = 25f;
    static float DAMPING = 5f;

    private final Vector2 focusPoint = new Vector2();
    private final GameCamera defaultCamera = new AgentCamera();
    private GameCamera camera = defaultCamera;

    private Location location;
    protected Body body;
    private final float radius;

    private WeaponSentry weaponSentry = new WeaponSentry();

    public enum Direction {
        Up, Left, Down, Right
    }

    public enum Activity {
        Idle, Explore, Combat, Cast, Thrust, Swipe, Death
    }

    AgentInfo info;
    Activity activity = Activity.Explore;
    Direction direction = Direction.Down;
    private final Map<Activity, Map<Direction, Animation>> animations;
    float stateTime = 0;

    private final MotionTracker motionTracker = new MotionTracker(this);
    private final List<Agent> neighbors = new ArrayList<>();
    private final Set<Agent> visibleNeighbors = new HashSet<>();
    private final Map<Agent, Boolean> lineOfSightCache = new HashMap<>();
    private final Map<Locatable, Float> distanceCache = new HashMap<>();
    private final LinkedList<Action> actions = new LinkedList<>();
    private final List<Effect> effects = new LinkedList<>();
    private final Set<ActivationHandler> activationHandlers = new HashSet<>();
    private Action action = null;

    private Agent followed = null;

    // hostilities: agents with negative reaction who have attacked us
    private final Set<Agent> assaulters = new HashSet<>(); // assaulters attack those who have
                                                           // no enemies
    private final Map<Agent, Float> relations = new HashMap<>();

    private int confused = 0;
    private int paralyzed = 0;
    private int imploding = 0;
    private int stunted = 0;
    private int crimes = 0;
    private boolean sprinting = false;

    private float freezing = 0;
    private float lastAction = 0;

    private boolean aiming = false;
    private float velocityPenalty = 0;

    private Agent target;
    private Interactable interactor;
    private Lootable looting = null;
    private Conversable converser = null;
    private boolean uploading = false;
    private boolean forcedDialogue;

    private final LinkedList<Announcement> announcements = Lists.newLinkedList();
    private final Set<String> uniqueDialogue = Sets.newHashSet();
    private final Set<Class<?>> toggles = new HashSet<Class<?>>();
    private final Set<ProjectileHandler> projectileHandlers = new HashSet<ProjectileHandler>();
    private final LineOfSightHandler losHandler = new LineOfSightHandler();
    private final TargetingHandler targetingHandler = new TargetingHandler();

    private final Color color = new Color(1, 1, 1, 1);

    private short lastMask;
    private Optional<AgentHandler> collisionDelegate = Optional.absent();

    public Agent(ActorParams params, boolean unique, float x, float y, float width, float height,
            Location location, Map<Activity, Map<Direction, Animation>> animations) {
        this(x, y, width, height, location, animations);

        // health, level, augmentations, etc.
        this.info = new AgentInfo(this, params, unique);
    }

    public Agent(float x, float y, float width, float height, Profession profession, int level,
            Location location, Map<Activity, Map<Direction, Animation>> animations) {
        this(x, y, width, height, location, animations);

        // health, level, augmentations, etc.
        this.info = new AgentInfo(this, profession, level);
    }

    public Agent(float x, float y, float width, float height, Location location,
            Map<Activity, Map<Direction, Animation>> animations) {
        super(width, height);
        setPosition(x, y);
        this.animations = animations;

        radius = getBodyRadius();
        this.location = location;
        body = createBody(x, y, location.getWorld());
    }

    protected float getBodyRadius() {
        return Math.max(getWidth(), getHeight()) / 5;
    }

    protected void setWeaponSentry(WeaponSentry sentry) {
        this.weaponSentry = sentry;
    }

    public Body getBody() {
        return body;
    }
    
    protected short getCategoryBits() {
        return Settings.BIT_AGENT;
    }

    private Body createBody(float x, float y, World world) {
        CircleShape circleShape = new CircleShape();
        circleShape.setPosition(new Vector2());
        circleShape.setRadius(radius);

        BodyDef characterBodyDef = new BodyDef();
        characterBodyDef.position.set(x, y);
        characterBodyDef.type = BodyType.DynamicBody;
        Body body = world.createBody(characterBodyDef);

        FixtureDef charFixtureDef = new FixtureDef();
        charFixtureDef.density = getDensity();
        charFixtureDef.friction = 0.5f;
        charFixtureDef.restitution = 0.1f;
        charFixtureDef.shape = circleShape;
        charFixtureDef.filter.groupIndex = 0;

        Fixture fixture = body.createFixture(charFixtureDef);
        fixture.setUserData(this); // allow callbacks to owning Agent

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = getCategoryBits();
        filter.maskBits = Settings.BIT_ANYTHING;
        fixture.setFilterData(filter);

        body.setLinearDamping(DAMPING);
        body.setAngularDamping(10);

        circleShape.dispose();
        return body;
    }

    public boolean hasCollisionDelegate() {
        return collisionDelegate.isPresent();
    }

    public void setCollisionDelegate(AgentHandler delegate) {
        this.collisionDelegate = Optional.of(delegate);
        setCollisionMask(delegate.getCollisionMask());
    }

    public void removeCollisionDelegate() {
        this.collisionDelegate = Optional.absent();
        resetCollisionMask();
    }

    public AgentHandler getCollisionDelegate() {
        return collisionDelegate.get();
    }

    public abstract void changeMaxVelocity(float delta);

    public abstract void changeMaxAcceleration(float delta);

    public void setAiming(boolean aiming) {
        if (aiming != this.aiming) {
            addVelocityPenalty(AIMING_V_PENALTY * (aiming ? 1 : -1));
        }
        this.aiming = aiming;
    }

    public boolean isAiming() {
        return aiming;
    }

    public boolean isAimingAt(Agent other) {
        if (!isAiming()) {
            return false;
        }

        if (targetCast(weaponSentry.getPosition(), weaponSentry.getTargetingVector())) {
            return targetingHandler.isTargeting(other);
        }
        return true;
    }

    public RayTarget getTargeting(Vector2 source, Vector2 target) {
        targetCast(source, target);
        return new RayTarget(targetingHandler.getTargeting(), targetingHandler.getFraction());
    }

    private boolean targetCast(Vector2 source, Vector2 target) {
        targetingHandler.reset();

        if (source.equals(target)) {
            // if we don't do this check explicitly, we can get the following error:
            // Expression: r.LengthSquared() > 0.0f
            return false;
        }

        location.getWorld().rayCast(targetingHandler, source, target);
        return true;
    }

    public boolean canDodge() {
        return info.getEnergy() > DODGE_COST;
    }

    public void dodge(Vector2 direction) {
        if (canDodge()) {
            float s = getMaxLinearSpeed() / getBaseSpeed();
            applyForce(direction.cpy().scl(DODGE_SCALE * s));
            info.expend(DODGE_COST);
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.SWISH, getPosition());
        }
    }

    public float getBaseSpeed() {
        return getMaxLinearSpeed();
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public boolean canSprint() {
        return info.getEnergy() > SPRINT_COST;
    }

    public void sprint(boolean sprinting) {
        if (this.sprinting == sprinting) {
            // must be idempotent
            return;
        }

        if (sprinting) {
            if (canSprint()) {
                addEffect(new Sprinting(this, SPRINT_SCALE, SPRINT_COST));
                this.sprinting = true;
            }
        } else {
            this.sprinting = false;
        }
    }

    public SoundEffect getWalkingSound() {
        return SoundEffect.FOOTSTEP;
    }

    public abstract void scaleLinearVelocity(float s);

    public void addVelocityPenalty(float delta) {
        velocityPenalty += delta;
    }

    protected float getVelocityPenalty() {
        return velocityPenalty;
    }

    public Location getLocation() {
        return location;
    }

    public void setCamera(GameCamera camera) {
        this.camera = camera;
    }

    public void resetCamera() {
        this.camera = defaultCamera;
    }

    public boolean usingRemoteCamera() {
        return camera != defaultCamera;
    }

    public GameCamera getCamera() {
        return camera;
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

    public float dst2(Locatable other) {
        if (!distanceCache.containsKey(other)) {
            distanceCache.put(other, getPosition().dst2(other.getPosition()));
        }
        return distanceCache.get(other);
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

    public Set<Agent> getVisibleNeighbors() {
        return visibleNeighbors;
    }

    public boolean isCompletelyDead() {
        // agent is dead and their animation is finished
        return !isAlive()
                && animations.get(Activity.Death).get(direction).isAnimationFinished(stateTime);
    }

    public void kill() {
        info.setHealth(0);
    }

    public boolean isAlive() {
        return info.isAlive();
    }

    public float getHealth() {
        return info.getHealth();
    }

    public float damage(Damage damage) {
        return damage(damage, 1);
    }

    public float damage(Damage damage, float delta) {
        float value = damage.apply(this, delta);
        if (isAlive()) {
            Agent source = damage.getSource();
            addHostility(source, value);
            alertTo(source);
        }
        return damage(value);
    }

    protected float damage(float value) {
        setCloaked(false); // damage breaks cloaking
        return info.damage(value);
    }

    public float heal(float value) {
        return info.heal(value);
    }

    public void resurrect() {
        info.resetHealth();
        setRgb(0.4f, 0.4f, 0.7f);
        setCollisionMask(Settings.BIT_ANYTHING);
    }

    public boolean isVisible() {
        return !isCloaked();
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

    public void handleProjectile(HandledProjectile projectile) {
        boolean handled = false;
        for (ProjectileHandler handler : projectileHandlers) {
            boolean result = handler.handle(projectile);
            if (result) {
                handled = true;
                break;
            }
        }

        if (!handled) {
            projectile.apply(this);
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

    public void setImploding(boolean imploding) {
        this.imploding += imploding ? 1 : -1;
        if (imploding || this.imploding == 0) {
            if (imploding) {
                setRgb(0, 0, 1);
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

    public void setStunted(boolean stunted) {
        this.stunted += stunted ? 1 : -1;
    }

    public boolean isStunted() {
        return stunted > 0;
    }

    public void setCrime(boolean crime) {
        this.crimes += crime ? 1 : -1;
    }

    public boolean isCommittingCrime() {
        return crimes > 0;
    }

    public void addFollower(Agent follower) {
        if (isFollowing(follower)) {
            // bidirectional following relationships can produce nasty side effects like infinite
            // loops, so this case must be explicitly disallowed
            follower.removeFollower(this);
        }
        follower.setFollowing(this);
    }

    public void removeFollower(Agent follower) {
        follower.stopFollowing(this);
    }

    private void setFollowing(Agent actor) {
        followed = actor;
    }

    private void stopFollowing(Agent actor) {
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
            setAlpha(getCloakAlpha());
        } else {
            toggles.remove(Cloak.class);
            setAlpha(1);
        }
    }

    public float getCloakAlpha() {
        return 0;
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

    public void addActivationHandler(ActivationHandler handler) {
        activationHandlers.add(handler);
    }

    public void removeActivationHandler(ActivationHandler handler) {
        activationHandlers.remove(handler);
    }

    public boolean handle(Activator activator) {
        for (ActivationHandler handler : activationHandlers) {
            if (handler.handle(activator)) {
                return true;
            }
        }
        return false;
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

    public Vector2 getTargetingPosition() {
        return getRenderPosition();
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
        TextureRegion region = animations.get(Activity.Idle).get(Direction.Right).getKeyFrame(0);
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

    /**
     * Begin dialogue with oneself.
     */
    public void announce(String banter) {
        if (!canSpeak()) {
            return;
        }

        // do not enqueue repeat announcements
        if (announcements.isEmpty()) {
            announcements.add(new BasicAnnouncement(banter));
        }
    }

    public void announce(Announcement announcement) {
        announcements.add(announcement);
    }

    public void banterFor(Agent listener, Response greeting) {
        // greeting is validated
        List<BanterAnnouncement> banter = Lists.newArrayList();
        banter.add(new ResponseAnnouncement(this, listener, greeting));
        addChoiceFor(listener, greeting, banter);

        // now update successor pointers
        BanterAnnouncement last = null;
        for (BanterAnnouncement announcement : banter) {
            if (last != null) {
                last.setNext(announcement);
            }
            last = announcement;
        }

        // announce greeting
        announcements.add(banter.get(0));
    }

    private void addChoiceFor(Agent listener, Response response, List<BanterAnnouncement> banter) {
        Choice choice = getDialogueHandler().getChoiceFor(response, listener);
        if (choice != null) {
            // choices made by listener
            banter.add(new BanterAnnouncement(listener, choice.getText()));
            addResponseFor(listener, choice, banter);
        }
    }

    private void addResponseFor(Agent listener, Choice choice, List<BanterAnnouncement> banter) {
        Response response = getDialogueHandler().getResponseFor(choice, listener);
        if (response != null) {
            banter.add(new ResponseAnnouncement(this, listener, response));
            addChoiceFor(listener, response, banter);
        }
    }

    public Announcement getNextAnnouncement() {
        return announcements.remove();
    }

    public boolean hasAnnouncements() {
        return !announcements.isEmpty();
    }

    public boolean isUploading() {
        return uploading;
    }

    public void upload(boolean value) {
        this.uploading = value;
    }

    public void beginInteraction(Interactable other) {
        interact(other);
    }

    public void beginLooting(Lootable lootable) {
        interact(lootable);
        loot(lootable);
    }

    public void beginDialogue(Conversable converser) {
        interact(converser);
        converse(converser);
    }

    public boolean isLooting() {
        return looting != null;
    }

    public Lootable getLooting() {
        return looting;
    }

    private void interact(Interactable other) {
        interactor = other;
    }

    private void loot(Lootable lootable) {
        this.looting = lootable;
    }

    private void converse(Conversable converser) {
        this.converser = converser;
    }

    public boolean inForcedDialogue() {
        return interactor != null && forcedDialogue;
    }

    public void beginInteraction(Agent other, boolean forced) {
        this.forcedDialogue = forced;
        interact(other);
        other.interact(this);
        setCamera(other.defaultCamera);
        other.setCamera(defaultCamera);

        // loot or speak
        if (!other.isAlive()) {
            loot(other);
        } else {
            // bidirectional
            converse(other);
            other.converse(this);
        }
    }

    public void unforceDialogue() {
        forcedDialogue = false;
    }

    public void endDialogue() {
        if (inDialogue()) {
            endJointInteraction();
        }
    }

    protected Set<String> getUniqueDialogue() {
        return uniqueDialogue;
    }

    public void addDialogue(String id) {
        uniqueDialogue.add(id);
    }

    public boolean hasHeardDialogue(String id) {
        return uniqueDialogue.contains(id);
    }

    public void endJointInteraction() {
        if (interactor != null) {
            interactor.endInteraction();
            endInteraction();
        }
    }

    @Override
    public void endInteraction() {
        resetCamera();
        interact(null);
        converser = null;
        looting = null;
    }

    public Interactable getInteractor() {
        return interactor;
    }

    public boolean isInteracting() {
        return interactor != null;
    }

    @Override
    public boolean canInteract() {
        if (isAlive()) {
            // must be out of combat and out of dialogue
            return getThreat().isCalm() && !inDialogue();
        } else {
            // can loot
            return true;
        }
    }

    public boolean canInteract(Interactable other) {
        if (other == this || dst2(other) >= INTERACT_RANGE) {
            // regardless of anything else, these factors must be met
            return false;
        }
        return other.canInteract();
    }

    public boolean inDialogue() {
        return converser != null && converser.canConverse();
    }

    @Override
    public boolean canConverse() {
        return isAlive();
    }

    public Conversable getConverser() {
        return converser;
    }

    public void setFocusPoint(Vector2 point) {
        setFocusPoint(point.x, point.y);
    }

    public void setFocusPoint(float x, float y) {
        focusPoint.set(x, y);
    }

    public Vector2 getFocusPoint() {
        return focusPoint;
    }

    public abstract void alertTo(Agent target);

    public abstract ThreatMonitor<?> getThreat();

    public void locate(Agent other) {
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
        if (other == this) {
            // can always target ourselves
            return true;
        }
        if (!isNear(other)) {
            // not within distance constraint
            return false;
        }
        if (!visibleNeighbors.contains(other)) {
            // cannot see visibly
            return false;
        }
        return true;
    }

    public boolean isNeighbor(Agent other) {
        return neighbors.contains(other);
    }

    public boolean canKeepTarget(Agent other) {
        return isNear(other);
    }

    public boolean hasVisibilityTo(Agent other) {
        return hasLineOfSight(other) && visibleNeighbors.contains(other);
    }

    public boolean isVisible(Agent other) {
        if (other.isVisible()) {
            return true;
        } else {
            // perception is [0, 1], scaling how far we can be from an invisible target for us to
            // detect it
            float visibility = getVisibility();

            // confer a penalty to visibility based on the target's deception, but not too great
            // so that given two equal deception skills, it's easier to perceive than deceive
            visibility *= 1.0f - (other.info.getDeception() * 0.1f);

            // at worst, you should be able to detect someone right in front of you
            visibility = Math.max(visibility, 1.0f);

            return dst2(other) < visibility * visibility;
        }
    }

    public float getVisibility() {
        return info.getPerception() * UNMASK_RANGE;
    }

    public boolean isNear(Agent other) {
        return dst2(other) <= MAX_DST2;
    }

    public boolean canTargetProjectile(Agent other) {
        return hasLineOfSight(other);
    }

    public boolean hasLineOfSight(Agent other) {
        if (!lineOfSightCache.containsKey(other)) {
            losHandler.reset(other);
            lineOfSightCache.put(other, rayCast(other.body.getPosition()));
        }
        return lineOfSightCache.get(other);
    }

    public boolean hasLineOfSight(Vector2 target) {
        losHandler.reset(null);
        return rayCast(target);
    }

    public boolean hasLineOfSight(Vector2 source, Agent target) {
        losHandler.reset(target);
        return rayCast(source, target.getPosition());
    }

    private boolean rayCast(Vector2 target) {
        return rayCast(body.getPosition(), target);
    }

    private boolean rayCast(Vector2 source, Vector2 target) {
        if (source.equals(target)) {
            // if we don't do this check explicitly, we can get the following error:
            // Expression: r.LengthSquared() > 0.0f
            return true;
        }
        location.getWorld().rayCast(losHandler, source, target);
        return losHandler.hasLineOfSight();
    }

    public float getAttackScale(Agent other) {
        return info.getOffense() * (1.0f - other.getInfo().getDefense());
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

    public float getDamageScale(DamageType damage) {
        return 1;
    }

    protected abstract SoundEffect getDeathSound();

    protected void onDeath() {
        actions.clear();
        action = null;
        setTarget(null);
        toggles.clear();
        setRgb(1, 1, 1);
        setCollisionMask(Settings.BIT_STATIC);
        releaseFragments();
        announcements.clear();
        InvokenGame.SOUND_MANAGER.playAtPoint(getDeathSound(), getPosition(), 3f);
    }

    // add fragment temporary entities to the world in a radial pattern around the agent
    // for players, we must also assign the fragments to the nearest unique neighbor (if possible)
    // for persistence
    protected void releaseFragments() {
        int total = info.getInventory().getItemCount((Fragment.getInstance()));
        Fragment.release(location, getPosition(), total);
        info.getInventory().removeItem(Fragment.getInstance(), total);
    }

    protected final void setCollisionMask(short maskBits) {
        // update all fixtures
        for (Fixture fixture : body.getFixtureList()) {
            // collision filters
            Filter filter = fixture.getFilterData();
            if (filter.maskBits != maskBits) {
                lastMask = filter.maskBits;
                filter.maskBits = maskBits;
                fixture.setFilterData(filter);
            }
        }
    }
    
    private void resetCollisionMask() {
        setCollisionMask(lastMask);
    }

    protected void attemptTakeAction(float delta, Location location) {
        lastAction += delta;
        if (actionInProgress()) {
            // cannot act if another action is in progress
            return;
        }

        if (inForcedDialogue()) {
            // cannot act while in a forced dialogue situation
            return;
        }

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
            // no disorienting effects, so take conscious action
            lastAction = 0;
            takeAction(delta, location);
        }
    }

    public boolean assaultedBy(Agent other) {
        return assaulters.contains(other);
    }

    public void addHostility(Agent source, float magnitude) {
        if (!isAlive()) {
            return;
        }

        if (!getThreat().hasEnemies()) {
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
        if (isFollowing()) {
            if (getFollowed() == agent) {
                // followers are always allies of their leaders
                return 100;
            }

            // followers take this disposition queues from their leader
            return followed.getRelationNoFollow(agent);
        }

        return getRelationNoFollow(agent);
    }

    // does not consider transitive effects to avoid infinite loops
    private float getRelationNoFollow(Agent agent) {
        if (agent.isFollowing(this)) {
            // leaders likes their followers, and followers like each other
            return 100;
        }

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
        if (!isAlive() || !agent.isAlive()) {
            // no need to do this check in this case
            return;
        }

        if (!getThreat().hasEnemy(agent) && isEnemy(agent)) {
            // unfriendly, so mark them as an enemy
            getThreat().addEnemy(agent);

            if (assaultedBy(agent)) {
                // they attacked us, mark them as an assaulter
                // assault carriers a severe penalty for ranking factions
                changeFactionRelations(agent, ASSAULT_PENALTY);
            }

            // request help from allies
            if (visibleNeighbors.contains(agent)) {
                onHostility(agent);
                requestAssistance(agent);
            }
        }
    }

    private void requestAssistance(Agent enemy) {
        for (Agent agent : neighbors) {
            // broadcast a message that we're now in combat with this agent
            agent.notifyOfHostility(this, enemy);
        }
    }

    protected void onHostility(Agent enemy) {
        // does nothing
    }

    protected void notifyOfHostility(Agent source, Agent enemy) {
        // does nothing
    }

    public boolean isAlly(Agent other) {
        return Behavior.isAllyGiven(getRelation(other));
    }

    public boolean isEnemy(Agent other) {
        return Behavior.isEnemyGiven(getRelation(other));
    }

    @Override
    public void update(float delta, Location location) {
        if (delta == 0)
            return;
        stateTime += delta;

        // clear iteration caches
        lineOfSightCache.clear();
        distanceCache.clear();

        // update neighbors
        location.getNeighbors(this);
        visibleNeighbors.clear();
        for (Agent neighbor : neighbors) {
            if (isVisible(neighbor)) {
                visibleNeighbors.add(neighbor);
            }
        }

        // TODO: if we changed our natural position, then alert all neighbors to our presence
        // if they pass a detection check

        // restore energy in proportion to elapsed time
        if (!isStunted()) {
            float scale = isAiming() ? 1f : 2f;
            info.restore(delta * scale);
        }

        // update inventory
        info.getInventory().update(delta);

        updateHeading();
        if (aiming) {
            weaponSentry.update(delta);
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

                // update threat
                getThreat().update(delta);
            }
        } else if (activity != Activity.Death) {
            // kill the agent
            onDeath();
        }

        // remove target if we can no longer target them
        if (hasTarget() && !canKeepTarget(target)) {
            setTarget(null);
        }

        // set activity
        Activity last = activity;
        if (!isAlive()) {
            activity = Activity.Death;
        } else {
            if (motionTracker.isStanding()) {
                activity = Activity.Idle;
            } else {
                activity = Activity.Explore;
            }
        }

        // reset state if the activity was changed
        if (activity != last) {
            stateTime = 0;
        }

        motionTracker.update(delta);

        // do this separately so we can still get the standing state
        Locatable observed = null;
        if (inDialogue()) {
            // face the interacting agent
            observed = getInteractor();
        } else if (target != null && target != this) {
            // face the target
            observed = target;
        }

        if (observed != null) {
            float dx = observed.getPosition().x - getPosition().x;
            float dy = observed.getPosition().y - getPosition().y;
            setDirection(getDominantDirection(dx, dy));
        } else if (isAiming()) {
            // face the aimed direction
            setDirection(getDominantDirection(weaponSentry.direction.x, weaponSentry.direction.y));
        }

        // end interactions if outside range
        if (isInteracting() && dst2(getInteractor()) > INTERACT_RANGE && !forcedDialogue) {
            endJointInteraction();
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

    public Direction getDominantDirection(float x, float y) {
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
        } else if (motionTracker.isStanding() && activity == Activity.Explore) {
            stateTime = 0;
        }

        // apply color
        Batch batch = renderer.getBatch();
        if (isCloaked()) {
            batch.setColor(color);
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

            if (isCloaked()) {
                // blending results in invisible outfits unless we increase the alpha
                batch.setColor(color.r, color.g, color.b, Math.min(color.a * 5, 1));
            }
            outfit.render(this, activity, stateTime, renderer);
        } else {
            render(activity, direction, stateTime, renderer);
        }

        // restore original color
        if (isCloaked()) {
            batch.setColor(Color.WHITE);
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

        Batch batch = renderer.getBatch();
        batch.begin();
        draw(batch, frame, direction);
        batch.end();
    }

    protected void draw(Batch batch, TextureRegion frame, Direction direction) {
        float width = getWidth();
        float height = getHeight();
        batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
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

    public float getWeaponAccuracy() {
        // TODO separate weapon class
        return 0.65f;
    }

    @Override
    public AgentInventory getInventory() {
        return info.getInventory();
    }

    public AgentInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return info.getName();
    }

    public WeaponSentry getWeaponSentry() {
        return weaponSentry;
    }

    public boolean hasSentryReady() {
        return !weaponSentry.isFinished();
    }

    public void recoil() {
        // does nothing
    }

    @Override
    public float getZ() {
        return isAlive() ? super.getZ() : Float.POSITIVE_INFINITY;
    }

    protected void setLocation(Location location, float x, float y) {
        this.location = location;
        body = createBody(x, y, location.getWorld());
        position.set(x, y);
    }

    public abstract boolean canSpeak();

    protected abstract void takeAction(float delta, Location screen);

    private class TargetingHandler implements RayCastCallback {
        private final short mask = Settings.BIT_SHOOTABLE;
        private Agent targeting = null;
        private float fraction = 1;

        public boolean isTargeting(Agent other) {
            return targeting == other;
        }

        public Agent getTargeting() {
            return targeting;
        }

        public float getFraction() {
            return fraction;
        }

        public void reset() {
            targeting = null;
            fraction = 1;
        }

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            int result = isTargeting(fixture);
            if (result > 0) {
                this.fraction = fraction;
                return fraction;
            } else if (result == 0) {
                this.fraction = fraction;
            }
            return result;
        }

        private int isTargeting(Fixture fixture) {
            short category = fixture.getFilterData().categoryBits;
            if ((mask & category) == 0) {
                // no common bits, so these items don't collide -> continue
                return -1;
            }

            for (Fixture f : body.getFixtureList()) {
                if (fixture == f) {
                    // we cannot obstruct our own view -> continue
                    return -1;
                }
            }

            // check that the fixture belongs to another agent
            if (fixture.getUserData() != null && fixture.getUserData() instanceof Agent) {
                Agent agent = (Agent) fixture.getUserData();
                if (agent.isAlive()) {
                    targeting = agent;
                    return 1;
                } else {
                    // cannot be obstructed by the body of a dead agent -> continue
                    return -1;
                }
            }

            // whatever it is, it's not a target and it's in the way -> terminate
            return 0;
        }
    };

    public static class RayTarget {
        private final Agent target;
        private final float fraction;

        public RayTarget(Agent target, float fraction) {
            this.target = target;
            this.fraction = fraction;
        }

        public Agent getTarget() {
            return target;
        }

        public float getFraction() {
            return fraction;
        }
    }

    private class LineOfSightHandler implements RayCastCallback {
        private final short mask = Settings.BIT_SHOOTABLE;
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
            if (isObstruction(fixture)) {
                lineOfSight = false;
                return fraction;
            }

            // ignore this fixture and continue
            return -1;
        }

        private boolean isObstruction(Fixture fixture) {
            short category = fixture.getFilterData().categoryBits;
            if ((mask & category) == 0) {
                // no common bits, so these items don't collide
                return false;
            }

            for (Fixture f : body.getFixtureList()) {
                if (fixture == f) {
                    // we cannot obstruct our own view
                    return false;
                }
            }

            if (target != null) {
                for (Fixture f : target.body.getFixtureList()) {
                    if (fixture == f) {
                        // it's not an obstruction if it's the thing we're aiming at
                        return false;
                    }
                }
            }

            // check that the fixture belongs to another agent
            if (fixture.getUserData() != null && fixture.getUserData() instanceof Agent) {
                Agent agent = (Agent) fixture.getUserData();
                if (!agent.isAlive()) {
                    // cannot be obstructed by the body of a dead agent
                    return false;
                }
            }

            // whatever it is, it's in the way
            return true;
        }
    };

    public class WeaponSentry implements TemporaryEntity {
        private static final float RANGE = 15f;
        private static final float X_OFFSET = 0.25f;

        private final Map<Agent, Boolean> lineOfSightCache = new HashMap<Agent, Boolean>();
        protected final Vector2 position = new Vector2();
        protected final Vector2 direction = new Vector2(1, 0);
        protected final Vector2 tmp = new Vector2();

        // offset relative to the center of the agent so the gun appears at roughly hip level,
        // not at the face
        protected final Vector2 offset = new Vector2(0, 0.25f);

        public boolean hasLineOfSight(Agent target) {
            if (!lineOfSightCache.containsKey(target)) {
                Vector2 origin = getRenderPosition();
                tmp.set(target.getPosition()).sub(origin).nor().add(origin);
                lineOfSightCache
                        .put(target,
                                Agent.this.hasLineOfSight(target)
                                        && Agent.this.hasLineOfSight(tmp, target));
            }
            return lineOfSightCache.get(target);
        }

        public void update(float delta) {
            Vector2 origin = getRenderPosition();
            direction.set(getFocusPoint()).sub(origin).nor();
            updatePosition();
            clear();
        }

        @Override
        public void update(float delta, Location location) {
            if (!Agent.this.aiming) {
                rotateTowards(delta, getForwardVector());
                updatePosition();
            }
        }

        private void updatePosition() {
            Vector2 origin = getRenderPosition();
            position.set(origin.x + direction.x, origin.y + direction.y).sub(offset);
            if (Agent.this.getDirection() == Direction.Up) {
                position.add(X_OFFSET, 0);
            } else if (Agent.this.getDirection() == Direction.Down) {
                position.add(-X_OFFSET, 0);
            }
        }

        private void rotateTowards(float delta, Vector2 destination) {
            rotate(delta, getTheta(destination), ROTATION_SCALE);
        }

        protected void rotate(float delta, float theta, float rate) {
            direction.rotate(theta * delta * rate * info.getAttackModifier());
        }

        protected float getTheta(Vector2 destination) {
            float theta = direction.angle(destination);
            theta = Math.signum(theta) * Math.max(Math.abs(theta), 15f);
            return theta;
        }

        public void clear() {
            lineOfSightCache.clear();
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            if (getInventory().hasRangedWeapon()) {
                RangedWeapon weapon = getInventory().getRangedWeapon();
                weapon.render(position, direction, renderer);
            }
        }

        @Override
        public float getZ() {
            if (Agent.this.getDirection() == Direction.Up) {
                // draw the weapon just behind the agent
                return Agent.this.getZ() + Settings.EPSILON;
            }
            return Agent.this.getZ();
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        public Vector2 getDirection() {
            return direction;
        }

        public float getRange() {
            return RANGE;
        }

        public Vector2 getTargetingVector() {
            tmp.set(direction).scl(RANGE).add(position);
            return tmp;
        }

        @Override
        public boolean isFinished() {
            return !isToggled(HoldingWeapon.class);
        }

        @Override
        public void dispose() {
        }
    }

    /**
     * Rotates towards the focus point, but does not immediately set itself there for a smoother
     * effect.
     */
    public class RotatingWeaponSentry extends WeaponSentry {
        private final Vector2 destination = new Vector2();
        private float elapsed = 0;
        private float theta = 0;

        @Override
        public void update(float delta) {
            Vector2 origin = getRenderPosition();
            destination.set(getFocusPoint()).sub(origin).nor();

            // move direction towards destination
            elapsed += delta;
            if (elapsed > 0.5) {
                theta = getTheta(destination);
                elapsed = 0;
            }
            rotate(delta, theta, ROTATION_SCALE);

            position.set(origin.x + direction.x, origin.y + direction.y).sub(offset);
            clear();
        }
    }

    private class AgentCamera implements GameCamera {
        @Override
        public Vector2 getPosition() {
            return Agent.this.position;
        }
    }
}