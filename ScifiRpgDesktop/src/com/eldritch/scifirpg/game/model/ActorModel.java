package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eldritch.scifirpg.game.util.ActorMarshaller;
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Confidence;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;

public class ActorModel {
    private final ActorMarshaller actorMarshaller = new ActorMarshaller();
    private final Set<String> deadNpcs = new HashSet<>();
    private final DialogueVerifier dialogueVerifier = new DialogueVerifier();

    public List<NpcState> getActorsFor(ActorEncounter encounter) {
        List<NpcState> actors = new ArrayList<>();
        for (ActorScenario scenario : encounter.getScenarios()) {
            String id = scenario.getActorId();
            if (isAlive(id)) {
                NpcState actor = new NpcState(getActor(id), scenario);
                actors.add(actor);
            }
        }
        return actors;
    }

    public NonPlayerActor getActor(String id) {
        return actorMarshaller.readAsset(id);
    }
    
    public boolean isAlive(String id) {
        return !deadNpcs.contains(id);
    }

    public class NpcState extends ActorState {
        private final NonPlayerActor data;
        private final ActorScenario scenario;

        public NpcState(NonPlayerActor data, ActorScenario scenario) {
            super(data.getParams());
            this.data = data;
            this.scenario = scenario;
        }
        
        public Response getGreeting() {
            if (scenario.hasDialogue()) {
                Response greeting = getGreetingFor(scenario.getDialogue());
                if (greeting != null) {
                    return greeting;
                }
            }
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

        public boolean isUnique() {
            return data.getUnique();
        }

        public boolean canSpeak() {
            return data.getCanSpeak();
        }

        public DialogueTree getDialogueTree() {
            return data.getDialogue();
        }

        public Aggression getBaseAggression() {
            return data.getAggression();
        }

        public Assistance getBaseAssistance() {
            return data.getAssistance();
        }

        public Confidence getBaseConfidence() {
            return data.getConfidence();
        }

        public List<Trait> getTraits() {
            return data.getTraitList();
        }
    }

    public static class ActorState {
        private final ActorParams params;
        private final Map<Discipline, SkillState> skills = new HashMap<>();

        public ActorState(ActorParams params) {
            this.params = params;
            for (Skill skill : params.getSkillList()) {
                skills.put(skill.getDiscipline(), new SkillState(skill));
            }
        }

        public String getName() {
            return params.getName();
        }

        public Gender getGender() {
            return params.getGender();
        }

        public Profession getProfession() {
            return params.getProfession();
        }

        public int getLevel() {
            return params.getLevel();
        }

        public int getSkillLevel(Discipline d) {
            return skills.get(d).getLevel();
        }

        public List<Skill> getSkills() {
            return params.getSkillList();
        }

        public List<FactionStatus> getFactionStatus() {
            return params.getFactionStatusList();
        }

        public List<InventoryItem> getInventoryItems() {
            return params.getInventoryItemList();
        }

        public List<String> getKnownAugmentations() {
            return params.getKnownAugIdList();
        }
    }

    public static class SkillState {
        private int level;
        private int xp;

        public SkillState(Skill skill) {
            this.level = skill.getLevel();
            this.xp = skill.getXp();
        }

        public int getLevel() {
            return level;
        }

        public int getXp() {
            return xp;
        }
    }
    
    public static class ParsedResponse {
        private final Response response;
        private final String parsedText;
        
        public ParsedResponse(Response response, String parsedText) {
            this.response = response;
            this.parsedText = parsedText;
        }

        public Response getResponse() {
            return response;
        }

        public String getParsedText() {
            return parsedText;
        }
    }
    
    public class DialogueVerifier {
        public boolean isValid(Response r) {
            // TODO use prerequisites and the current actor model state
            return true;
        }
    }
}
