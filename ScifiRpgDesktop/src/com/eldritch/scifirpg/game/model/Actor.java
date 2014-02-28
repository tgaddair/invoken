package com.eldritch.scifirpg.game.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;

public class Actor {
    // Immutable actor fields
    private final ActorParams params;

    // Mutable actor fields
    private int level;
    private final Map<Discipline, SkillState> skills = new HashMap<>();
    private final Map<String, FactionState> factions = new HashMap<>();
    private final Map<String, ItemState> inventory = new HashMap<>();
    private final Set<String> knownAugmentations = new HashSet<>();
    private int health;

    // Game specific parameters not set during construction
    private final Set<InventoryItem> equipped = new HashSet<>();
    private final Set<StagedAugmentation> stagedAugmentations = new HashSet<>();

    public Actor(ActorParams params) {
        this.params = params;
        for (Skill skill : params.getSkillList()) {
            skills.put(skill.getDiscipline(), new SkillState(skill));
        }
        for (FactionStatus status : params.getFactionStatusList()) {
            factions.put(status.getFactionId(), new FactionState(status));
        }
        for (InventoryItem item : params.getInventoryItemList()) {
            inventory.put(item.getItemId(), new ItemState(item));
        }
        for (String knownAug : params.getKnownAugIdList()) {
            knownAugmentations.add(knownAug);
        }
        
        level = params.getLevel();
        health = getBaseHealth();
    }
    
    public final void equip(String itemId) {
        equipped.add(inventory.get(itemId).getItem());
    }
    
    public final void stage(StagedAugmentation aug) {
        stagedAugmentations.add(aug);
    }
    
    public void levelUp() {
        // TODO
        level++;
    }

    public int getLevel() {
        return params.getLevel();
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getCurrentHealth() {
        return health;
    }
    
    public int getBaseHealth() {
        return skills.get(Discipline.WARFARE).getLevel();
    }
    
    public String getName() {
        return params.getName();
    }
    
    public Species getSpecies() {
        return params.getSpecies();
    }

    public Gender getGender() {
        return params.getGender();
    }

    public Profession getProfession() {
        return params.getProfession();
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

    public static class FactionState {
        private final FactionStatus status;

        public FactionState(FactionStatus status) {
            this.status = status;
        }
    }

    public static class ItemState {
        private final InventoryItem item;

        public ItemState(InventoryItem item) {
            this.item = item;
        }
        
        public InventoryItem getItem() {
            return item;
        }
    }
}