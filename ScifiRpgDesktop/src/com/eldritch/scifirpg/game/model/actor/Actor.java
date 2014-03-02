package com.eldritch.scifirpg.game.model.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.game.util.AugmentationMarshaller;
import com.eldritch.scifirpg.game.util.ItemMarshaller;
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
import com.eldritch.scifirpg.proto.Items.Item;

public abstract class Actor {
    protected final static AugmentationMarshaller AUG_READER = new AugmentationMarshaller();
    protected final static ItemMarshaller ITEM_READER = new ItemMarshaller();

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
    private final Set<Item> equipped = new HashSet<>();
    private final Set<AugmentationState> stagedAugmentations = new HashSet<>();

    // Game specific parameters not saved to disk
    // ActionAugmentation must not override equals() for this to work
    private final Set<ActionAugmentation> actionBuffer = new LinkedHashSet<>();

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

    public boolean isAlive() {
        return health > 0;
    }
    
    public void changeHealth(int magnitude) {
        if (magnitude >= 0) {
            heal(magnitude);
        } else {
            damage(magnitude);
        }
    }

    public int heal(int magnitude) {
        // Can't heal more than our maximum health
        int value = Math.min(magnitude, getBaseHealth() - health);
        health = value;
        return value;
    }

    public Set<ActionAugmentation> getActions() {
        return actionBuffer;
    }

    public int damage(DamageType type, int magnitude) {
        // TODO handle resistances
        return damage(magnitude);
    }
    
    public int damage(int magnitude) {
        // Can't do more damage than the target has health
        int damage = Math.min(magnitude, health);
        health -= damage;
        System.out.println(getName() + ": " + health);
        return damage;
    }

    public void removeAction(ActionAugmentation aug) {
        actionBuffer.remove(aug);
    }

    public Set<ActionAugmentation> redrawActions() {
        actionBuffer.clear();
        return drawActions();
    }

    public Set<ActionAugmentation> drawActions() {
        Set<ActionAugmentation> drawn = new LinkedHashSet<>();
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
     * Denotes reaction time. Higher initiative results in higher turn order in
     * combat.
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
    
    protected SkillState getSkill(Discipline d) {
        return skills.get(d);
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

    public String getId() {
        return params.getId();
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

    public boolean hasEquipped(String itemId) {
        if (!inventory.containsKey(itemId)) {
            return false;
        }
        Item item = inventory.get(itemId).getItem();
        return equipped.contains(item);
    }

    public int getItemCount(String itemId) {
        if (!inventory.containsKey(itemId)) {
            return 0;
        }
        return inventory.get(itemId).getCount();
    }

    public void changeItemCount(String itemId, int count) {
        if (count >= 0) {
            addItem(itemId, count);
        } else {
            removeItem(itemId, Math.abs(count));
        }
    }

    public void addItem(String itemId, int count) {
        if (!inventory.containsKey(itemId)) {
            inventory.put(itemId, new ItemState(ITEM_READER.readAsset(itemId), count));
        } else {
            inventory.get(itemId).add(count);
        }
    }

    /**
     * Remove the requested number of instances of the given item from the
     * actor's inventory. If the number requested is greater than or equal to
     * the number available, or if count == -1, then we remove all and unequip.
     */
    public void removeItem(String itemId, int count) {
        int available = getItemCount(itemId);
        if (available == 0) {
            // Nothing to remove
            return;
        }
        
        if (count >= available || count == -1) {
            // Remove all and unequip
            Item item = inventory.get(itemId).getItem();
            equipped.remove(item);
            inventory.remove(itemId);
        } else {
            // Decrement counters
            inventory.get(itemId).remove(count);
        }
    }
    
    public void changeReputation(String factionId, int value) {
        if (!factions.containsKey(factionId)) {
            factions.put(factionId, new FactionState(factionId));
        }
        factions.get(factionId).changeReputation(value);
    }

    public int getReputation(String faction) {
        if (!factions.containsKey(faction)) {
            return 0;
        }
        return factions.get(faction).getReputation();
    }

    public boolean hasRank(String faction) {
        // TODO add further check for rank existence within faction
        return factions.containsKey(faction);
    }

    public int getRank(String faction) {
        if (!factions.containsKey(faction)) {
            return 0;
        }
        return factions.get(faction).getRank();
    }

    public Collection<String> getKnownAugmentations() {
        return knownAugmentations;
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
        
        public void addXp(int delta) {
            xp += delta;
        }

        public int getXp() {
            return xp;
        }
    }

    public static class FactionState {
        private final String factionId;
        private int reputation;
        private int rank;
        
        public FactionState(String factionId) {
            this.factionId = factionId;
            this.reputation = 0;
            this.rank = -1;
        }

        public FactionState(FactionStatus status) {
            this.factionId = status.getFactionId();
            this.reputation = status.getReputation();
            this.rank = status.hasRank() ? status.getRank() : -1;
        }

        public int getReputation() {
            return reputation;
        }
        
        public void changeReputation(int delta) {
            // Limit the amount we can increase reputation to be <= than 10 * (rank + 2)
            int max = 10 * (getRank() + 2);
            reputation = Math.min(reputation + delta, max);
        }

        public void setReputation(int reputation) {
            this.reputation = reputation;
        }
        
        public boolean hasRank() {
            return rank >= 0;
        }

        public int getRank() {
            return rank;
        }
        
        public void promote() {
            rank++;
            reputation += 10;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getFactionId() {
            return factionId;
        }
    }

    public static class ItemState {
        private final Item item;
        private int count;
        
        public ItemState(Item item, int count) {
            this.item = item;
            this.count = count;
        }

        public ItemState(InventoryItem item) {
            this.item = ITEM_READER.readAsset(item.getItemId());
            count = item.getCount();
        }

        public Item getItem() {
            return item;
        }
        
        public void add(int c) {
            count += c;
        }
        
        public void remove(int c) {
            // Can't have negative count
            count = Math.max(count - c, 0);
        }

        public int getCount() {
            return count;
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
            augmentation = AUG_READER.readAsset(augId);
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

    public abstract boolean hasEnemy();
}