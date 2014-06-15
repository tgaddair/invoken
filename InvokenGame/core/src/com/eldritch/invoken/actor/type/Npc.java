package com.eldritch.invoken.actor.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.Inventory.ItemState;
import com.eldritch.invoken.actor.ai.AssaultRoutine;
import com.eldritch.invoken.actor.ai.AssistRoutine;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.ai.FleeRoutine;
import com.eldritch.invoken.actor.ai.FollowRoutine;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.Pathfinder;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.util.PrerequisiteVerifier;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.proto.Prerequisites.Standing;
import com.google.common.base.Optional;

public abstract class Npc extends Agent {
	private final NonPlayerActor data;
    private final Optional<ActorScenario> scenario;
	private final DialogueVerifier dialogueVerifier = new DialogueVerifier();
	private final Behavior behavior;
	final Pathfinder pathfinder = new Pathfinder();
	private final List<Routine> routines = new ArrayList<Routine>();
	private final Set<Agent> detected = new HashSet<Agent>();
	
	// used in AI routine calculations to determine things like the target
	private final List<Agent> neighbors = new ArrayList<Agent>();
	private Routine routine;
	
	public Npc(NonPlayerActor data, float x, float y, float width, float height,
	        Map<Activity, Map<Direction, Animation>> animations, Location location) {
		super(data.getParams(), x, y, width, height, animations);
		this.data = data;
		scenario = Optional.absent();
		behavior = new Behavior(this, data);
		
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
		if (routine.isFinished()) {
			for (Routine r : routines) {
				if (r.isValid()) {
					setRoutine(r);
					break;
				}
			}
		} else {
			for (Routine r : routines) {
				if (r.canInterrupt() && r.isValid()) {
					if (r != routine) {
						setRoutine(r);
					}
					break;
				}
			}
		}
		routine.takeAction(delta, screen);
	}
	
	@Override
    public void setConfused(boolean confused) {
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
	
	@Override
	public boolean inCombat() {
        return super.inCombat() || !detected.isEmpty();
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
