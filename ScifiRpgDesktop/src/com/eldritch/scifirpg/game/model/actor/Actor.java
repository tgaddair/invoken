package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.game.util.AugmentationMarshaller;
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Effects.DamageType;

public abstract class Actor {
    protected final static AugmentationMarshaller marshaller = new AugmentationMarshaller();
    
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
    private final Set<AugmentationState> stagedAugmentations = new HashSet<>();

    // Game specific parameters not saved to disk
    private final List<ActionAugmentation> actionBuffer = new ArrayList<>();

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
    
    public int heal(int magnitude) {
        // Can't heal more than our maximum health
        int value = Math.min(magnitude, getBaseHealth());
        health += value;
        return value;
    }
    
    public int damage(DamageType type, int magnitude) {
        // TODO handle resistances
        
        // Can't do more damage than the target has health
        int damage = Math.min(magnitude, health);
        health -= damage;
        return damage;
    }

    public List<ActionAugmentation> redrawActions() {
        actionBuffer.clear();
        return drawActions();
    }

    public List<ActionAugmentation> drawActions() {
        List<ActionAugmentation> drawn = new ArrayList<>();
        boolean canDraw = true;
        int slots = getBufferSlots();
        while (canDraw && actionBuffer.size() < slots) {
            Augmentation aug = drawAvailableAugmentation();
            if (aug != null) {
                ActionAugmentation action = new ActionAugmentation(aug, this);
                actionBuffer.add(action);
                drawn.add(action);
            } else {
                canDraw = false;
            }
        }
        return drawn;
    }
    
    protected Collection<AugmentationState> getStagedAugmentations() {
        return stagedAugmentations;
    }
    
    private Augmentation drawAvailableAugmentation() {
        int total = 0;
        for (AugmentationState augState : stagedAugmentations) {
            if (augState.getRemainingUses() > 0) {
                total += augState.getWeight();
            }
        }
        
        double target = Math.random() * total;
        double sum = 0.0;
        for (AugmentationState augState : stagedAugmentations) {
            sum += augState.getWeight();
            if (sum > target) {
                return augState.getAugmentation();
            }
        }
        
        return null;
    }

    /**
     * Three buffer slots by default, plus one additional slot for every 25
     * points in Automata.
     */
    public int getBufferSlots() {
        int bonus = skills.get(Discipline.AUTOMATA).getLevel() / 25;
        return bonus + 3;
    }

    /**
     * Number of hit points is exactly equal to the Warfare skill.
     */
    public int getBaseHealth() {
        return skills.get(Discipline.WARFARE).getLevel();
    }
    
    /**
     * Denotes reaction time.  Higher initiative results in higher turn order
     * in combat.
     */
    public int getInitiative() {
        return skills.get(Discipline.SUBTERFUGE).getLevel();
    }

    public final void equip(String itemId) {
        equipped.add(inventory.get(itemId).getItem());
    }

    public final void stage(StagedAugmentation aug) {
        stage(new AugmentationState(aug));
        if (!knownAugmentations.contains(aug.getAugId())) {
            knownAugmentations.add(aug.getAugId());
        }
    }
    
    public final void stage(AugmentationState augState) {
        stagedAugmentations.add(augState);
    }

    public void levelUp() {
        // TODO
        level++;
    }

    public int getLevel() {
        return level;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getCurrentHealth() {
        return health;
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
        private final String factionId;
        private int reputation;
        private int rank;

        public FactionState(FactionStatus status) {
            this.factionId = status.getFactionId();
            this.reputation = status.getReputation();
            this.rank = status.getRank();
        }

        public int getReputation() {
            return reputation;
        }

        public void setReputation(int reputation) {
            this.reputation = reputation;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getFactionId() {
            return factionId;
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
    
    public static class AugmentationState {
        private final String augId;
        private final Augmentation augmentation;
        private int stages;
        private int remainingUses;
        
        public AugmentationState(Augmentation augmentation, int stages) {
            this.augId = augmentation.getId();
            this.stages = stages;
            this.remainingUses = stages;
            this.augmentation = augmentation;
        }
        
        public AugmentationState(StagedAugmentation aug) {
            this.augId = aug.getAugId();
            this.stages = aug.getStages();
            this.remainingUses = aug.getRemainingUses();
            augmentation = marshaller.readAsset(augId);
        }
        
        public Augmentation getAugmentation() {
            return augmentation;
        }
        
        public int getWeight() {
            // TODO maybe weight augs dynamically
            return remainingUses;
        }

        public int getStages() {
            return stages;
        }

        public void setStages(int stages) {
            this.stages = stages;
        }

        public int getRemainingUses() {
            return remainingUses;
        }

        public void setRemainingUses(int remainingUses) {
            this.remainingUses = remainingUses;
        }

        public String getAugId() {
            return augId;
        }
        
        public String getName() {
            return augmentation.getName();
        }
    }
    
    public abstract void takeCombatTurn(ActorEncounterModel model);
    
    /**
     * Returns true if the attack succeeded, false if the attack was countered.
     */
    public abstract boolean handleAttack(ActionAugmentation attack);
}