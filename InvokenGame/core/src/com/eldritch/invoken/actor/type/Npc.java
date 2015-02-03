package com.eldritch.invoken.actor.type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Flee;
import com.badlogic.gdx.ai.steer.behaviors.Hide;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.steer.utils.Ray;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.ConversationHandler.DialogueVerifier;
import com.eldritch.invoken.actor.Inventory.ItemState;
import com.eldritch.invoken.actor.ai.AdaptiveRayWithWhiskersConfiguration;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.ai.Box2dRaycastCollisionDetector;
import com.eldritch.invoken.actor.ai.NpcState;
import com.eldritch.invoken.actor.ai.NpcStateMachine;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;
import com.eldritch.invoken.proto.Prerequisites.Standing;
import com.eldritch.invoken.util.PrerequisiteVerifier;
import com.google.common.base.Optional;

public abstract class Npc extends SteeringAgent implements Telegraph {
	private final NonPlayerActor data;
    private final Optional<ActorScenario> scenario;
    private final ConversationHandler dialogue;
	private final Behavior behavior;
	private final Set<Agent> detected = new HashSet<Agent>();
	private final BasicSteerable lastSeen = new BasicSteerable();
	
	// used in AI routine calculations to determine things like the target
	private final Set<Agent> squad = new HashSet<Agent>();
	
	// AI controllers
	private final NpcStateMachine stateMachine;
	private boolean canAttack = true;
	
	RayConfigurationBase<Vector2> rayConfiguration;
	
	// behaviors that need to be updated periodically
	private final Hide<Vector2> hide;
	private final Evade<Vector2> evade;
	private final Pursue<Vector2> pursue;
	private final Flee<Vector2> flee;
	private final Seek<Vector2> seek;
	private final Arrive<Vector2> arrive;
	private final Wander<Vector2> wander;
	
	public Npc(NonPlayerActor data, float x, float y, float width, float height, float maxVelocity,
	        Map<Activity, Map<Direction, Animation>> animations, Location location) {
		super(data.getParams(), x, y, width, height, maxVelocity, location, animations);
		this.data = data;
		scenario = Optional.absent();
		dialogue = new ConversationHandler(data.getDialogue(), new NpcDialogueVerifier());
		behavior = new Behavior(this, data);
		
		// equip items
		for (ItemState item : info.getInventory().getItems()) {
			info.getInventory().equip(item.getItem());
		}
		
		// steering behaviors
		rayConfiguration = new AdaptiveRayWithWhiskersConfiguration<Vector2>(this, 3, 1, 35 * MathUtils.degreesToRadians);
		RaycastObstacleAvoidance<Vector2> obstacleAvoidance = new RaycastObstacleAvoidance<Vector2>(
				this, 
				rayConfiguration,
				new Box2dRaycastCollisionDetector(location.getWorld()),
				1);
		
		// ally proximity
		// TODO: move this to Location, have a set of "squads" managed at a higher level with a shared proximity instance
//		Proximity<Vector2> proximity = new Proximity<Vector2>() {
//			private Steerable<Vector2> owner = Npc.this;
//			
//			@Override
//			public Steerable<Vector2> getOwner() {
//				return owner;
//			}
//
//			@Override
//			public void setOwner(Steerable<Vector2> owner) {
//				this.owner = owner;
//			}
//
//			@Override
//			public int findNeighbors(
//					com.badlogic.gdx.ai.steer.Proximity.ProximityCallback<Vector2> callback) {
//				int count = 0;
//				for (Agent neighbor : Npc.this.getNeighbors()) {
//					if (neighbor.dst2(Npc.this) < 5) {
//						if (Behavior.isAllyGiven(getRelation(neighbor))) {
//							callback.reportNeighbor(neighbor);
//						}
//					}
//				}
//				return count;
//			}
//			
//		};
//		Cohesion<Vector2> cohesion = new Cohesion<Vector2>(this, proximity);
		
		hide = new Hide<Vector2>(this);
		evade = new Evade<Vector2>(this, location.getPlayer());
		pursue = new Pursue<Vector2>(this, location.getPlayer());
		flee = new Flee<Vector2>(this);
		seek = new Seek<Vector2>(this);
		arrive = new Arrive<Vector2>(this).setArrivalTolerance(3f).setDecelerationRadius(5f);
		wander = new Wander<Vector2>(this)
				// Don't use Face internally because independent facing is off
				.setFaceEnabled(false) //
				// We don't need a limiter supporting angular components because Face is not used
				// No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for the same reason
				.setLimiter(new LinearAccelerationLimiter(5))
				.setWanderOffset(2)
				.setWanderOrientation(0)
				.setWanderRadius(0.5f)
				.setWanderRate(MathUtils.PI / 5);
		
		// initially disable our states
		hide.setEnabled(false);
		evade.setEnabled(false);
		pursue.setEnabled(false);
		flee.setEnabled(false);
		seek.setEnabled(false);
		arrive.setEnabled(false);
		
		// order in descending priority
		PrioritySteering<Vector2> prioritySteering = new PrioritySteering<Vector2>(this)
				.add(obstacleAvoidance)
//				.add(cohesion)
				.add(hide)
				.add(evade)
				.add(pursue)
				.add(flee)
				.add(seek)
				.add(arrive)
				.add(wander);
		setBehavior(prioritySteering);
		
		// state machine
		stateMachine = new NpcStateMachine(this, NpcState.PATROL);
		stateMachine.changeState(NpcState.PATROL);
	}
	
	@Override
	public boolean handleMessage (Telegram telegram) {
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
	
	public boolean isThreatened() {
		return hasEnemies();
	}
	
	public boolean isAgitated() {
	    if (!isAlive()) {
	        return false;
	    }
		return behavior.shouldAssault(getVisibleNeighbors());
	}
	
	public boolean isCombatReady() {
	    return isThreatened() || isAgitated();
	}
	
	@Override
	protected void takeAction(float delta, Location screen) {
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
		stateMachine.update(delta);
		if (steeringBehavior != null) {
	        // Calculate steering acceleration
	        steeringBehavior.calculateSteering(steeringOutput);
	
	        // Apply steering acceleration to move this agent
	        applySteering(steeringOutput, delta);
	    }
	}
	
	public void render(OrthographicCamera camera) {
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		Ray<Vector2>[] rays = rayConfiguration.getRays();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(1, 0, 0, 1);
		shapeRenderer.setProjectionMatrix(camera.combined);
		for (int i = 0; i < rays.length; i++) {
			Ray<Vector2> ray = rays[i];
			Vector2 end = ray.origin.cpy().add(ray.direction);
			shapeRenderer.line(ray.origin, end);
		}
		
		shapeRenderer.end();
	}
	
	@Override
	protected void onDeath() {
		super.onDeath();
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
	
	@Override
	public void setTarget(Agent other) {
	    super.setTarget(other);
	    if (other != null) {
	        detected.add(other);
	        lastSeen.setPosition(other.getPosition());
	        setFocusPoint(other.getPosition());
	    }
	}
	
	@Override
    public boolean isVisible(Agent other) {
	    // ignore FOV check if we're already in combat with the agent, as it's primarily for
	    // handling stealth
        return super.isVisible(other) && hasLineOfSight(other) 
                && (hostileTo(other) || inFieldOfView(other));
    }
	
	public BasicSteerable getLastSeen() {
        return lastSeen;
    }
	
	public boolean inFieldOfView(Agent other) {
		// field of view: compute angle between character-character and forward vectors
        Vector2 a = getForwardVector();
        Vector2 b = other.position.cpy().sub(position).nor();
        double theta = Math.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y);
        return Math.abs(theta) <= Math.PI / 2;
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
    
    public int getInfluence(Agent other) {
        // TODO
        return 0;
    }
    
    public Standing getStanding(Agent other) {
        // TODO
        return Standing.NEUTRAL;
    }
    
    public class NpcDialogueVerifier extends PrerequisiteVerifier implements DialogueVerifier {
        @Override
        protected boolean verifyInfluence(Prerequisite prereq, Agent actor) {
            int value = getInfluence(actor);
            return verifyBetween(prereq, value);
        }
        
        @Override
        protected boolean verifyStanding(Prerequisite prereq, Agent actor) {
            Standing value = getStanding(actor);
            boolean has = value == Standing.valueOf(prereq.getTarget());
            return verifyHas(prereq, has);
        }
        
        public boolean isValid(Response r) {
//            return verify(r.getPrereqList(), model);
        	return true;
        }
        
        public boolean isValid(Choice c) {
//            return verify(c.getPrereqList(), model);
        	return true;
        }
    }
    
    public static Npc create(NonPlayerActor data, float x, float y, Location location) {
        Species species = data.getParams().getSpecies();
        switch (species) {
            case HUMAN:
                return new HumanNpc(data, x, y, "sprite/characters/male-fair.png", location);
            case UNDEAD:
                return new Undead(data, x, y, "sprite/characters/hollow-zombie.png", location);
            case AUTOMATON:
                return new Automaton(data, x, y, "sprite/characters/automaton/mech1", location);
            case HOLLOW:
                return new Hollow(data, x, y, "sprite/characters/hollow/golem", location);
            default:
                throw new IllegalArgumentException("unrecognized species: " + species);
        }
    }
}
