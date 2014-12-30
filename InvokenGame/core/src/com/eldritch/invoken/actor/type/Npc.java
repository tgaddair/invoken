package com.eldritch.invoken.actor.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.CollisionAvoidance;
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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.Inventory.ItemState;
import com.eldritch.invoken.actor.ai.AdaptiveRayWithWhiskersConfiguration;
import com.eldritch.invoken.actor.ai.AssaultRoutine;
import com.eldritch.invoken.actor.ai.AssistRoutine;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.ai.Box2dRaycastCollisionDetector;
import com.eldritch.invoken.actor.ai.FleeRoutine;
import com.eldritch.invoken.actor.ai.FollowRoutine;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.NpcState;
import com.eldritch.invoken.actor.ai.NpcStateMachine;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.actor.pathfinding.Pathfinder;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Actors.DialogueTree;
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
	private final DialogueVerifier dialogueVerifier = new DialogueVerifier();
	private final Behavior behavior;
	final Pathfinder pathfinder = new Pathfinder();
	private final List<Routine> routines = new ArrayList<Routine>();
	private final Set<Agent> detected = new HashSet<Agent>();
	
	// used in AI routine calculations to determine things like the target
	private final Location location;
	private final List<Agent> neighbors = new ArrayList<Agent>();
	private Routine routine;
	
	// AI controllers
	private final NpcStateMachine stateMachine;
	
	private final Map<Class<? extends SteeringBehavior<?>>, SteeringBehavior<Vector2>> behaviors = 
			new LinkedHashMap<Class<? extends SteeringBehavior<?>>, SteeringBehavior<Vector2>>();
	RayConfigurationBase<Vector2> rayConfiguration;
	
	// behaviors that need to be updated periodically
	private final Hide<Vector2> hide;
	private final Evade<Vector2> evade;
	private final Pursue<Vector2> pursue;
	private final Flee<Vector2> flee;
	private final Seek<Vector2> seek;
	private final Wander<Vector2> wander;
	
	public Npc(NonPlayerActor data, float x, float y, float width, float height,
	        Map<Activity, Map<Direction, Animation>> animations, Location location) {
		super(data.getParams(), x, y, width, height, location.getWorld(), animations);
		this.data = data;
		scenario = Optional.absent();
		behavior = new Behavior(this, data);
		this.location = location;
		
		// equip items
		for (ItemState item : info.getInventory().getItems()) {
			info.getInventory().equip(item.getItem());
		}
		
		// routines
		// TODO: add these to proto to make them modular for NPCs
		routines.add(new FleeRoutine(this, location));
		routines.add(new AssaultRoutine(this, location));
		routines.add(new AssistRoutine(this, location));
		routines.add(new FollowRoutine(this));
		
		Routine idle = new IdleRoutine(this);
        Routine patrol = new PatrolRoutine(this);
		routines.add(patrol);
		routines.add(idle); // idle is fallback
		
		routine = Math.random() < 0.5 ? idle : patrol;
		
		// steering behaviors
		rayConfiguration = new AdaptiveRayWithWhiskersConfiguration<Vector2>(this, 3, 1, 35 * MathUtils.degreesToRadians);
		RaycastObstacleAvoidance<Vector2> obstacleAvoidance = new RaycastObstacleAvoidance<Vector2>(
				this, 
				rayConfiguration,
				new Box2dRaycastCollisionDetector(location.getWorld()),
				1);
		Proximity<Vector2> proximity = new Proximity<Vector2>() {
			private Steerable<Vector2> owner = Npc.this;
			
			@Override
			public Steerable<Vector2> getOwner() {
				return owner;
			}

			@Override
			public void setOwner(Steerable<Vector2> owner) {
				this.owner = owner;
			}

			@Override
			public int findNeighbors(
					com.badlogic.gdx.ai.steer.Proximity.ProximityCallback<Vector2> callback) {
				int count = 0;
				for (Agent neighbor : getNeighbors()) {
					if (neighbor instanceof SteeringAgent) {
						callback.reportNeighbor((SteeringAgent) neighbor);
						count++;
					}
				}
				return count;
			}
		};
		CollisionAvoidance<Vector2> collisionAvoidance = new CollisionAvoidance<Vector2>(this, proximity);
		hide = new Hide<Vector2>(this);
		evade = new Evade<Vector2>(this, location.getPlayer());
		pursue = new Pursue<Vector2>(this, location.getPlayer());
		flee = new Flee<Vector2>(this);
		seek = new Seek<Vector2>(this);
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
		
		// order in descending priority
		PrioritySteering<Vector2> prioritySteering = new PrioritySteering<Vector2>(this)
				.add(obstacleAvoidance)
//				.add(collisionAvoidance)
				.add(hide)
				.add(evade)
				.add(pursue)
				.add(flee)
				.add(seek)
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
	
	public Wander<Vector2> getWander() {
		return wander;
	}
	
	public boolean isThreatened() {
		return hasEnemies() || !detected.isEmpty();
	}
	
	public boolean isSafe() {
		return !isThreatened();
	}
	
	public Location getLocation() {
		return location;
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
	
	@Override
	protected void takeAction(float delta, Location screen) {
		// update neighbors
		screen.getNeighbors(this);
		
		// update detected set
		Iterator<Agent> it = detected.iterator();
		while (it.hasNext()) {
		    Agent other = it.next();
		    if (!other.isAlive() || dst2(other) > MAX_DST2 / 2) {
		        it.remove();
            }
		}
		
		// update routine
//		if (routine.isFinished()) {
//			for (Routine r : routines) {
//				if (r.isValid()) {
//					setRoutine(r);
//					break;
//				}
//			}
//		} else {
//			for (Routine r : routines) {
//				if (r.canInterrupt() && r.isValid()) {
//					if (r != routine) {
//						setRoutine(r);
//					}
//					break;
//				}
//			}
//		}
//		routine.takeAction(delta, screen);
		
		// update steering
		update(delta);
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
	    }
	}
	
	public boolean canTarget(Agent other, Location location) {
        // within distance constraint
        if (!super.canTarget(other, location)) {
            return false;
        }
        
        // if we're hostile, then we can target when within range
        if (hostileTo(other)) {
            return true;
        }
        
        // recently seen and nearby
        if (detected.contains(other)) {
            return true;
        }

        // view obstruction: intersect character-character segment with collision tiles
        int startX = (int) Math.floor(Math.min(position.x, other.position.x));
        int startY = (int) Math.floor(Math.min(position.y, other.position.y));
        int endX = (int) Math.ceil(Math.max(position.x, other.position.x));
        int endY = (int) Math.ceil(Math.max(position.y, other.position.y));
        Array<Rectangle> tiles = location.getTiles(startX, startY, endX, endY);
        Vector2 tmp = new Vector2();
        for (Rectangle tile : tiles) {
            float r = Math.max(tile.width, tile.height);
            if (Intersector
                    .intersectSegmentCircle(position, other.position, tile.getCenter(tmp), r)) {
                return false;
            }
        }

        return true;
    }
	
	public boolean inFieldOfView(Agent other) {
	 // field of view: compute angle between character-character and forward vectors
        Vector2 a = getForwardVector();
        Vector2 b = other.position.cpy().sub(position).nor();
        double theta = Math.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y);
        return Math.abs(theta) <= Math.PI / 2;
	}
	
	public List<Agent> getNeighbors() {
	    return neighbors;
	}
	
	public Behavior getBehavior() {
	    return behavior;
	}
	
	public Pathfinder getPathfinder() {
	    return pathfinder;
	}
	
	public Vector2 getClearTarget(Location screen) {
		return getClearTarget(getTarget().getPosition(), screen);
	}
	
	public Vector2 getClearTarget(Vector2 target, Location screen) {
		return pathfinder.getTarget(this, getPosition(), target, screen);
	}
	
	public Vector2 getClearTarget(double theta, Location screen) {
	    Vector2 target = pathfinder.rotate(getTarget().getPosition(), getPosition(), theta);
        return pathfinder.getTarget(this, getPosition(), target, screen);
    }
	
	private void setRoutine(Routine routine) {
		this.routine = routine;
		pathfinder.reset();
		routine.reset();
	}
	
	@Override
	public void handleInteract(Agent other) {
		other.interact(this);
	}
	
	public List<Choice> getChoicesFor(Response response) {
        List<Choice> choices = new ArrayList<Choice>();
        for (Choice choice : response.getChoiceList()) {
            if (dialogueVerifier.isValid(choice)) {
                choices.add(choice);
            }
        }
        return choices;
    }
    
    public Response getResponseFor(Choice choice) {
        Set<String> successors = new HashSet<String>(choice.getSuccessorIdList());
        for (Response r : data.getDialogue().getDialogueList()) {
            if (successors.contains(r.getId()) && dialogueVerifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }
    
    public boolean hasGreeting() {
        // TODO this could be more efficient
        return getGreeting() != null;
    }
    
    public Response getGreeting() {
//        if (scenario.hasDialogue()) {
//            Response greeting = getGreetingFor(scenario.getDialogue());
//            if (greeting != null) {
//                return greeting;
//            }
//        }
        return getGreetingFor(data.getDialogue());
    }
    
    private Response getGreetingFor(DialogueTree tree) {
        for (Response r : tree.getDialogueList()) {
            if (r.getGreeting() && dialogueVerifier.isValid(r)) {
                return r;
            }
        }
        return null;
    }
    
    public int getInfluence(Agent other) {
        // TODO
        return 0;
    }
    
    public Standing getStanding(Agent other) {
        // TODO
        return Standing.NEUTRAL;
    }
    
    public class DialogueVerifier extends PrerequisiteVerifier {
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
