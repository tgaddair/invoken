package com.eldritch.invoken.actor.type;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Flee;
import com.badlogic.gdx.ai.steer.behaviors.Hide;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.ConversationHandler.DialogueVerifier;
import com.eldritch.invoken.actor.ai.AdaptiveRayWithWhiskersConfiguration;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.ai.FatigueMonitor;
import com.eldritch.invoken.actor.ai.IntimidationMonitor;
import com.eldritch.invoken.actor.ai.NpcState;
import com.eldritch.invoken.actor.ai.NpcStateMachine;
import com.eldritch.invoken.actor.ai.NpcThreatMonitor;
import com.eldritch.invoken.actor.ai.Squad;
import com.eldritch.invoken.actor.ai.TacticsManager;
import com.eldritch.invoken.actor.ai.btree.Combat;
import com.eldritch.invoken.actor.ai.btree.HandleThreat;
import com.eldritch.invoken.actor.ai.btree.Investigate;
import com.eldritch.invoken.actor.ai.btree.Patrol;
import com.eldritch.invoken.actor.ai.btree.Speak;
import com.eldritch.invoken.actor.ai.btree.SquadTactics;
import com.eldritch.invoken.actor.ai.planning.Planner;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.type.NavigatedSteerable.AStarNavigatedSteerable;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.box2d.Box2dRaycastCollisionDetector;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.OutcomeHandler;
import com.eldritch.invoken.util.PrerequisiteVerifier;
import com.google.common.base.Optional;

public abstract class Npc extends SteeringAgent implements Telegraph {
    public static final float STEP = 0.01f; // behavior action frequency
    private static final float SIGHTED_DURATION = 1f; // time enemy is in sights before firing
    
    private static final float ALERT_DST2 = 3f * 3f;
    private static final float MIN_DST2 = 1f;

    public enum SteeringMode {
        Default, Wander, Pursue, Evade, Follow
    }

    private final NonPlayerActor data;
    private final Optional<ActorScenario> scenario;
    private final ConversationHandler dialogue;
    private final Set<Agent> detected = new HashSet<Agent>();
    private final NavigatedSteerable lastSeen;
    private final Behavior behavior;

    // used in AI routine calculations to determine things like the target
    private Optional<Squad> squad = Optional.absent();

    // AI controllers
    private final BehaviorTree<Npc> behaviorTree;
    private FixedPoint cover = null;
    private final NpcThreatMonitor threat;
    private final FatigueMonitor fatigue;
    private final IntimidationMonitor intimidation;
    private final TacticsManager tactics;
    private final Planner planner;
    private Augmentation chosenAug;
    private float lastStep = 0;
    private float sighted = 0;
    private float lastDialogue = 0;

    private final NpcStateMachine stateMachine;
    private boolean canAttack = true;

    private final Map<SteeringMode, SteeringBehavior<Vector2>> behaviors;
    private final RaycastCollisionDetector<Vector2> collisionDetector;
    RayConfigurationBase<Vector2> rayConfiguration;

    // behaviors that need to be updated periodically
    private Hide<Vector2> hide;
    private Evade<Vector2> evade;
    private Pursue<Vector2> pursue;
    private Flee<Vector2> flee;
    private Seek<Vector2> seek;
    private Arrive<Vector2> arrive;
    private Wander<Vector2> wander;

    // debug
    private String lastTask = "";

    public Npc(NonPlayerActor data, Optional<ActorScenario> scenario, float x, float y,
            float width, float height, float maxVelocity,
            Map<Activity, Map<Direction, Animation>> animations, Level level) {
        super(data.getParams(), data.getUnique(), x, y, width, height, maxVelocity, level,
                animations);
        this.data = data;
        this.scenario = scenario;
        dialogue = new ConversationHandler(data.getDialogueList(), new NpcDialogueVerifier(),
                new NpcOutcomeHandler());
        behavior = new Behavior(this, data);

        // add random fragments proportional to the current level
        info.getInventory().addItem(Fragment.getInstance(), getFragments(info.getLevel()));

        // equip items
        for (ItemState item : info.getInventory().getItems()) {
            info.getInventory().equipIfBetter(item.getItem());
        }

        // pathfinding
        lastSeen = createSteerable(level);

        // steering behaviors
        collisionDetector = createCollisionDetector(level);
        behaviors = getSteeringBehaviors(level);
        setBehavior(behaviors.get(SteeringMode.Default));

        // state machine
        stateMachine = new NpcStateMachine(this, NpcState.PATROL);
        stateMachine.changeState(NpcState.PATROL);

        // behavior tree
        behaviorTree = new BehaviorTree<Npc>(createBehavior(), this);
        threat = new NpcThreatMonitor(this);
        fatigue = new FatigueMonitor(this);
        intimidation = new IntimidationMonitor(this);
        tactics = new TacticsManager(this);
        planner = Planner.from(this);

        // post init
        setWeaponSentry(new RotatingWeaponSentry());
    }

    public boolean hasScenario() {
        return scenario.isPresent();
    }

    public ActorScenario getScenario() {
        return scenario.get();
    }

    protected NavigatedSteerable createSteerable(Level level) {
        return new AStarNavigatedSteerable(this, level);
    }

    protected RaycastCollisionDetector<Vector2> createCollisionDetector(Level level) {
        return new Box2dRaycastCollisionDetector(level.getWorld());
    }

    public void setSquad(Squad squad) {
        this.squad = Optional.of(squad);
    }

    public boolean hasSquad() {
        return squad.isPresent();
    }

    public Squad getSquad() {
        return squad.get();
    }

    public FixedPoint getCover() {
        return cover;
    }

    public void setChosen(Augmentation aug) {
        this.chosenAug = aug;
    }

    public Augmentation getChosen() {
        return chosenAug;
    }

    public boolean hasChosen() {
        return chosenAug != null;
    }

    @Override
    public void setAiming(boolean aiming) {
        super.setAiming(aiming);
        if (!aiming) {
            // lose sights
            sighted = 0;
        }
    }

    public FatigueMonitor getFatigue() {
        return fatigue;
    }

    public IntimidationMonitor getIntimidation() {
        return intimidation;
    }

    public TacticsManager getTactics() {
        return tactics;
    }

    public Planner getPlanner() {
        return planner;
    }

    @Override
    public NpcThreatMonitor getThreat() {
        return threat;
    }

    public void setTask(String taskName) {
        if (!lastTask.isEmpty()) {
            lastTask += " -> ";
        }
        lastTask += taskName;
    }

    public String getLastTask() {
        return lastTask;
    }

    public static Task<Npc> createBehavior() {
        Selector<Npc> selector = new Selector<Npc>();
        selector.addChild(new Combat());
        selector.addChild(new HandleThreat());
        selector.addChild(new Investigate());
        selector.addChild(new Speak());
        selector.addChild(new SquadTactics());
        selector.addChild(new Patrol());
        return selector;
    }

    @Override
    public boolean handleMessage(Telegram telegram) {
        return stateMachine.handleMessage(telegram);
    }

    public NpcStateMachine getStateMachine() {
        return stateMachine;
    }

    public void setCanAttack(boolean attack) {
        canAttack = attack;
    }

    public boolean canAttack() {
        return canAttack;
    }

    public void setBehavior(SteeringMode mode) {
        if (mode == null) {
            setBehavior(behaviors.get(SteeringMode.Default));
        }
        setBehavior(behaviors.get(mode));
    }

    public Hide<Vector2> getHide() {
        return hide;
    }

    public Evade<Vector2> getEvade() {
        return evade;
    }

    public Pursue<Vector2> getPursue() {
        return pursue;
    }

    public Flee<Vector2> getFlee() {
        return flee;
    }

    public Seek<Vector2> getSeek() {
        return seek;
    }

    public Arrive<Vector2> getArrive() {
        return arrive;
    }

    public Wander<Vector2> getWander() {
        return wander;
    }

    public float getLastDialogue() {
        return lastDialogue;
    }

    @Override
    public void beginInteraction(Agent other, boolean forced) {
        super.beginInteraction(other, forced);
        lastDialogue = 0;
    }

    public boolean isThreatened() {
        return threat.hasEnemies();
    }

    public boolean isAgitated() {
        if (!isAlive()) {
            return false;
        }
        return behavior.shouldAssault(getVisibleNeighbors());
    }

    public boolean isCombatReady() {
        return threat.isCombatReady();
    }

    @Override
    protected void takeAction(float delta, Level screen) {
        // update detected set
        Iterator<Agent> it = detected.iterator();
        while (it.hasNext()) {
            Agent other = it.next();
            if (!other.isAlive() || dst2(other) > MAX_DST2 / 2) {
                it.remove();
            }
        }

        // update relations
        for (Agent neighbor : getVisibleNeighbors()) {
            getRelation(neighbor);
        }

        // update steering
        update(delta);
    }

    public void update(float delta) {
        if (!inDialogue()) {
            lastDialogue += delta;
        }

        // update tactics
        tactics.update(delta);
        planner.plan(delta);

        // update sighted info
        lastSeen.update(delta);
        if (hasTarget() && hasLineOfSight(getTarget())) {
            sighted += delta;
            getIntimidation().useUntilLimit(delta);
        } else {
            sighted = 0;
            getIntimidation().useUntilLimit(-delta);
        }
        if (hasTarget() && !hasVisibilityTo(getTarget())) {
            // lose sight
            sighted = 0;
        }

        // action planning
        lastStep += delta;
        if (lastStep > STEP) {
            lastTask = "";
            behaviorTree.step();
            lastStep = 0;
        }

        // steering and movement
        if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

            // Apply steering acceleration to move this agent
            applySteering(steeringOutput, delta);
        }
    }

    @Override
    protected void updateDirection(Locatable observed) {
        // face the aimed direction
        WeaponSentry weaponSentry = getWeaponSentry();
        setDirection(getDominantDirection(weaponSentry.direction.x, weaponSentry.direction.y));
    }

    public void render(OrthographicCamera camera) {
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        Ray<Vector2>[] rays = rayConfiguration.getRays();
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.setProjectionMatrix(camera.combined);
        for (int i = 0; i < rays.length; i++) {
            Ray<Vector2> ray = rays[i];
            shapeRenderer.line(ray.start, ray.end);
        }

        shapeRenderer.end();
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        threat.clear();
        detected.clear();
        stateMachine.changeState(NpcState.PATROL);
    }

    @Override
    protected void handleConfusion(boolean confused) {
        if (confused) {
            behavior.setAggression(Aggression.FRENZIED);
        } else {
            behavior.resetAggression();
        }
    }

    public boolean hasSights() {
        return sighted > SIGHTED_DURATION;
    }

    public boolean isSuspicious() {
        return threat.isSuspicious();
    }

    public boolean isGuard() {
        return data.getGuard();
    }

    public void alertAllies(Agent target) {
        for (Agent neighbor : getNeighbors()) {
            if (isAlly(neighbor)) {
                // nearby ally
                neighbor.suspicionTo(target);
            }
        }
    }

    @Override
    protected void onHostility(Agent enemy) {
        threat.setAlerted(enemy);
    }

    @Override
    protected void notifyOfHostility(Agent source, Agent target) {
        if (isAlive() && behavior.shouldAssist(source, target)) {
            threat.addEnemy(target);
            threat.setAlerted();
        }
    }

    @Override
    public boolean isEnemy(Agent other) {
        return behavior.wantsToAttack(other);
    }

    @Override
    public void alertTo(Agent other) {
        // set a waypoint to investigate
        tactics.setWaypoint(Investigate.class, other.getPosition());

        lastSeen.setPosition(other);
        setFocusPoint(other.getTargetingPosition());
        setTarget(other);
        threat.addEnemy(other);
        threat.setAlerted();
    }

    @Override
    public void suspicionTo(Agent other) {
        // set a waypoint to investigate
        tactics.setWaypoint(Investigate.class, other.getPosition());

        if (!hasTarget()) {
            lastSeen.setPosition(other);
            setFocusPoint(other.getTargetingPosition());
            setTarget(other);
            threat.maybeSetSuspicious();
        } else if (other == getTarget()) {
            lastSeen.setPosition(other);
            setFocusPoint(other.getTargetingPosition());
            threat.maybeSetSuspicious();
        }

        // when we're alerted to an enemy, we should treat this like assisting ourselves
        if (behavior.wantsToAttack(other, true)) {
            threat.addEnemy(other);
        }
    }

    @Override
    public void locate(Locatable other) {
        lastSeen.locate(other);
    }

    @Override
    public void setTarget(Agent other) {
        if (other != getTarget()) {
            // changed targets so reset
            sighted = 0;
            chosenAug = null;
        }

        super.setTarget(other);
        if (other != null) {
            detected.add(other);
            lastSeen.setPosition(other);
            setFocusPoint(other.getTargetingPosition());
        }
    }

    @Override
    public boolean isVisible(Agent other) {
        // ignore FOV check if we're already in combat with the agent, as it's primarily for
        // handling stealth
        return super.isVisible(other) && hasLineOfSight(other)
                && (inFieldOfView(other) || canHear(other));
    }

    private boolean canHear(Agent other) {
        if (threat.hostileTo(other)) {
            return dst2(other) < ALERT_DST2;
        }

        return dst2(other) < MIN_DST2;
    }

    public NavigatedSteerable getLastSeen() {
        return lastSeen;
    }

    @Override
    public double getFieldOfView() {
        float scale = threat.getAwareness();
        return super.getFieldOfView() * scale;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    @Override
    public boolean canSpeak() {
        return dialogue.canSpeak();
    }

    @Override
    public ConversationHandler getDialogueHandler() {
        return dialogue;
    }

    public class NpcDialogueVerifier extends PrerequisiteVerifier implements DialogueVerifier {
        @Override
        public boolean isValid(Response r, Agent interactor) {
            if (interactor.hasHeardDialogue(getId(r))) {
                // already heard this dialogue
                return false;
            }

            return verify(r.getPrereqList(), interactor);
        }

        @Override
        public boolean isValid(Choice c, Agent interactor) {
            return verify(c.getPrereqList(), interactor);
        }

        @Override
        protected Agent getSource() {
            return Npc.this;
        }
    }

    public class NpcOutcomeHandler extends OutcomeHandler {
        @Override
        protected Agent getSource() {
            return Npc.this;
        }

        @Override
        protected String getId(Response response) {
            return Npc.this.getId(response);
        }
    }

    private String getId(Response r) {
        return String.format("%s:%s", info.getId(), r.getId());
    }

    public class CoverProximity implements Proximity<Vector2> {
        @Override
        public Steerable<Vector2> getOwner() {
            return Npc.this;
        }

        @Override
        public void setOwner(Steerable<Vector2> owner) {
            // does nothing
        }

        @Override
        public int findNeighbors(ProximityCallback<Vector2> callback) {
            cover = null;
            int count = 0;
            if (getHide().getTarget() != null && !getLocation().getActiveCover().isEmpty()) {
                Steerable<Vector2> target = getHide().getTarget();
                FixedPoint bestCover = null;
                boolean bestLos = false;
                float bestDistance = Float.POSITIVE_INFINITY;

                for (FixedPoint coverPoint : getLocation().getActiveCover()) {
                    Vector2 position = coverPoint.getPosition();
                    if (!getLocation().hasLineOfSight(Npc.this,
                            getHide().getTarget().getPosition(), position)) {
                        boolean los = hasLineOfSight(position);
                        float distance = position.dst2(getPosition());
                        if (target.getPosition().dst2(position) < distance) {
                            // enemy in the way
                            continue;
                        }

                        if (los && !bestLos) {
                            // any cover point we can see is immediately better
                            bestLos = true;
                            bestDistance = distance;
                            bestCover = coverPoint;
                        } else if (los == bestLos) {
                            // use distance comparison
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                bestCover = coverPoint;
                            }
                        }
                    }
                }

                // only report the best cover
                if (bestCover != null) {
                    cover = bestCover;
                    callback.reportNeighbor(bestCover);
                    count++;
                }
            }
            return count;
        }
    }

    public static Npc create(NonPlayerActor data, float x, float y, Level level) {
        return create(data, Optional.<ActorScenario> absent(), x, y, level);
    }

    public static Npc create(NonPlayerActor data, Optional<ActorScenario> scenario, float x,
            float y, Level level) {
        Species species = data.getParams().getSpecies();
        switch (species) {
            case HUMAN:
                return new HumanNpc(data, scenario, x, y, level);
            case UNDEAD:
                return Undead.from(data, scenario, x, y, level);
            case AUTOMATON:
                return Automaton.from(data, scenario, x, y, level);
            case BEAST:
                return Beast.from(data, scenario, x, y, level);
            case HOLLOW:
                return Hollow.from(data, scenario, x, y, level);
            default:
                throw new IllegalArgumentException("unrecognized species: " + species);
        }
    }

    private Map<SteeringMode, SteeringBehavior<Vector2>> getSteeringBehaviors(Level level) {
        Map<SteeringMode, SteeringBehavior<Vector2>> behaviors = new EnumMap<SteeringMode, SteeringBehavior<Vector2>>(
                SteeringMode.class);

        rayConfiguration = new AdaptiveRayWithWhiskersConfiguration<Vector2>(this, 3, 1,
                35 * MathUtils.degreesToRadians);
        RaycastObstacleAvoidance<Vector2> obstacleAvoidance = new RaycastObstacleAvoidance<Vector2>(
                this, rayConfiguration, collisionDetector, 1f);

        // ally proximity
        // TODO: move this to Location, have a set of "squads" managed at a higher level with a
        // shared proximity instance
        // Proximity<Vector2> proximity = new Proximity<Vector2>() {
        // private Steerable<Vector2> owner = Npc.this;
        //
        // @Override
        // public Steerable<Vector2> getOwner() {
        // return owner;
        // }
        //
        // @Override
        // public void setOwner(Steerable<Vector2> owner) {
        // this.owner = owner;
        // }
        //
        // @Override
        // public int findNeighbors(
        // com.badlogic.gdx.ai.steer.Proximity.ProximityCallback<Vector2> callback) {
        // int count = 0;
        // for (Agent neighbor : Npc.this.getNeighbors()) {
        // if (neighbor.dst2(Npc.this) < 5) {
        // if (Behavior.isAllyGiven(getRelation(neighbor))) {
        // callback.reportNeighbor(neighbor);
        // }
        // }
        // }
        // return count;
        // }
        //
        // };
        // Cohesion<Vector2> cohesion = new Cohesion<Vector2>(this, proximity);

        hide = new Hide<Vector2>(this, null, new CoverProximity()).setArrivalTolerance(.0001f)
                .setDecelerationRadius(.001f).setDistanceFromBoundary(0f);
        evade = new Evade<Vector2>(this, level.getPlayer())
                .setLimiter(new LinearAccelerationLimiter(getDefaultAcceleration()));
        pursue = new Pursue<Vector2>(this, level.getPlayer());
        flee = new Flee<Vector2>(this);
        seek = new Seek<Vector2>(this);
        arrive = new Arrive<Vector2>(this).setArrivalTolerance(3f).setDecelerationRadius(5f);
        wander = new Wander<Vector2>(this)
                // Don't use Face internally because independent facing is off
                .setFaceEnabled(false)
                //
                // We don't need a limiter supporting angular components because Face is not used
                // No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for
                // the same reason
                .setLimiter(new LinearAccelerationLimiter(getDefaultAcceleration() / 2))
                .setWanderOffset(2).setWanderOrientation(0).setWanderRadius(0.5f)
                .setWanderRate(MathUtils.PI / 5);
        Wander<Vector2> combatWander = new Wander<Vector2>(this).setFaceEnabled(false)
                .setLimiter(new LinearAccelerationLimiter(getDefaultAcceleration()))
                .setWanderOffset(3).setWanderOrientation(0).setWanderRadius(2)
                .setWanderRate(MathUtils.PI / 5);

        // initially disable our states
        // hide.setEnabled(false);
        // evade.setEnabled(false);
        // pursue.setEnabled(false);
        // flee.setEnabled(false);
        // seek.setEnabled(false);
        // arrive.setEnabled(false);

        // order in descending priority
        SteeringBehavior<Vector2> idleSteering = new PrioritySteering<Vector2>(this)
                .setLimiter(NullLimiter.NEUTRAL_LIMITER);
        behaviors.put(SteeringMode.Default, idleSteering);

        SteeringBehavior<Vector2> wanderSteering = new PrioritySteering<Vector2>(this)
                .setLimiter(NullLimiter.NEUTRAL_LIMITER) //
                .add(obstacleAvoidance) //
                .add(wander);
        behaviors.put(SteeringMode.Wander, wanderSteering);

        SteeringBehavior<Vector2> blendedPursuing = new BlendedSteering<Vector2>(this).add(pursue,
                2f).add(combatWander, getCombatWander());
        // .add(wander, 1f);
        SteeringBehavior<Vector2> pursueSteering = new PrioritySteering<Vector2>(this)
                .setLimiter(NullLimiter.NEUTRAL_LIMITER) //
                .add(obstacleAvoidance) //
                .add(blendedPursuing);
        behaviors.put(SteeringMode.Pursue, pursueSteering);

        SteeringBehavior<Vector2> blendedEvading = new BlendedSteering<Vector2>(this).add(hide,
                0.75f) //
                .add(evade, 0.25f);
        SteeringBehavior<Vector2> evadeSteering = new PrioritySteering<Vector2>(this)
                .setLimiter(NullLimiter.NEUTRAL_LIMITER) //
                .add(obstacleAvoidance) //
                .add(blendedEvading);
        behaviors.put(SteeringMode.Evade, evadeSteering);

        SteeringBehavior<Vector2> followSteering = new PrioritySteering<Vector2>(this)
                .setLimiter(NullLimiter.NEUTRAL_LIMITER) //
                .add(obstacleAvoidance) //
                .add(arrive);
        behaviors.put(SteeringMode.Follow, followSteering);

        return behaviors;
    }

    protected float getCombatWander() {
        return 2f;
    }

    private static int getFragments(int level) {
        int max = AgentInfo.getFragmentRequirement(level) / 5;
        return (int) (Math.random() * max);
    }
}
