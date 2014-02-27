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

    public List<NpcState> getActorsFor(ActorEncounter encounter) {
        List<NpcState> actors = new ArrayList<>();
        for (ActorScenario scenario : encounter.getScenarios()) {
            String id = scenario.getActorId();
            if (isAlive(id)) {
                actors.add(getActor(id));
            }
        }
        return actors;
    }

    public NpcState getActor(String id) {
        return new NpcState(actorMarshaller.readAsset(id));
    }
    
    public boolean isAlive(String id) {
        return !deadNpcs.contains(id);
    }

    public static class NpcState extends ActorState {
        private final NonPlayerActor data;

        public NpcState(NonPlayerActor data) {
            super(data.getParams());
            this.data = data;
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
}
