package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Inventory.ItemState;
import com.eldritch.invoken.actor.ai.AttackRoutine;
import com.eldritch.invoken.actor.ai.Behavior;
import com.eldritch.invoken.actor.ai.FollowRoutine;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.Pathfinder;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.util.PrerequisiteVerifier;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.proto.Prerequisites.Standing;
import com.google.common.base.Optional;

public class Npc extends Agent {
	private final NonPlayerActor data;
    private final Optional<ActorScenario> scenario;
	private final DialogueVerifier dialogueVerifier = new DialogueVerifier();
	private final Behavior behavior;
	final Pathfinder pathfinder = new Pathfinder();
	private final Map<Agent, Float> relations = new HashMap<Agent, Float>();
	private final List<Routine> routines = new ArrayList<Routine>();
	private Routine routine;
	
	public Npc(NonPlayerActor data, float x, float y, String asset) {
		super(asset, x, y, data.getParams());
		this.data = data;
		scenario = Optional.absent();
		behavior = new Behavior(this, data);
		
		// equip items
		for (ItemState item : info.getInventory().getItems()) {
			info.getInventory().equip(item.getItem());
		}
		
		// construct augs and items by randomly sampling from available
//        for (String augId : info.getKnownAugmentations()) {
//            Augmentation aug = InvokenGame.AUG_READER.readAsset(augId);
//            stage(new ActiveAugmentation(aug, this, 20));
//        }
		
		// routines
		// TODO: add these to proto to make them modular for NPCs
		routine = new IdleRoutine(this);
		routines.add(new AttackRoutine(this));
		routines.add(new FollowRoutine(this));
		routines.add(new PatrolRoutine(this));
		routines.add(routine); // idle is fallback
	}
	
	@Override
	protected void takeAction(float delta, Location screen) {
		// cleanup state
		Iterator<Entry<Agent, Float>> it = relations.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Agent, Float> entry = it.next();
			if (!entry.getKey().isAlive()) {
				it.remove();
			}
		}
		
		// update dispositions for neighbors
		for (Agent agent : screen.getActors()) {
			if (dst2(agent) < 50) {
				if (agent != this && agent.isAlive()) {
					if (!relations.containsKey(agent)) {
						relations.put(agent, getDisposition(agent));
					}
					
					// add enemies and allies
					if (relations.get(agent) < 0) {
						addEnemy(agent);
					}
				}
			}
		}
		
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
		return pathfinder.getTarget(getPosition(), target, screen);
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
	
	public Map<Agent, Float> getRelations() {
		return relations;
	}
	
	@Override
	protected void onDeath() {
		super.onDeath();
		relations.clear();
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
}
