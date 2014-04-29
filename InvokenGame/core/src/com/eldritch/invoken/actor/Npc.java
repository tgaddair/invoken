package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.ai.AttackRoutine;
import com.eldritch.invoken.actor.ai.FollowRoutine;
import com.eldritch.invoken.actor.ai.IdleRoutine;
import com.eldritch.invoken.actor.ai.PatrolRoutine;
import com.eldritch.invoken.actor.ai.Routine;
import com.eldritch.invoken.actor.aug.FireWeapon;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.PrerequisiteVerifier;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.proto.Prerequisites.Standing;
import com.google.common.base.Optional;

public class Npc extends Agent {
	private final NonPlayerActor data;
    private final Optional<ActorScenario> scenario;
	private final DialogueVerifier dialogueVerifier = new DialogueVerifier();
	private final Map<Agent, Float> relations = new HashMap<Agent, Float>();
	private final List<Routine> routines = new ArrayList<Routine>();
	private Routine routine;
	
	public Npc(NonPlayerActor data, float x, float y, String asset) {
		super(asset, x, y, data.getParams());
		this.data = data;
		scenario = Optional.absent();
		
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
	
	public Npc(Profession profession, int level, float x, float y, String asset) {
		super(asset, x, y, profession, level);
		data = null;
		scenario = Optional.absent();
		
		// routines
		routine = new IdleRoutine(this);
		routines.add(new AttackRoutine(this));
		routines.add(new FollowRoutine(this));
		routines.add(new PatrolRoutine(this));
		routines.add(routine); // idle is fallback
	}
	
	@Override
	protected void takeAction(float delta, GameScreen screen) {
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
//			Gdx.app.log(InvokenGame.LOG, "FINISHED");
			for (Routine r : routines) {
				if (r.isValid()) {
//					Gdx.app.log(InvokenGame.LOG, "routine: " + r);
					routine = r;
					routine.reset();
					break;
				}
			}
		} else {
			for (Routine r : routines) {
				if (r.canInterrupt() && r.isValid()) {
					if (r != routine) {
						routine = r;
						routine.reset();
					}
					break;
				}
			}
		}
		routine.takeAction(delta, screen);
	}
	
	@Override
	public void handleInteract(Agent other) {
		other.setDialogue(this);
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
