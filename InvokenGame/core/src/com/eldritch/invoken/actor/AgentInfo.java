package com.eldritch.invoken.actor;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.eldritch.invoken.actor.Inventory.ItemState;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.factions.FactionManager;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.invoken.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.invoken.proto.Actors.ActorParams.Skill;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Augmentations.AugmentationProto;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.google.common.base.Functions;

public class AgentInfo {
    private static final float BASE_ENERGY_RATE = 3f;

    final String id;
    final String name;
    final Species species;
    final boolean unique;

    final Profession profession;
    final FactionManager factions;
    private final Inventory inventory = new Inventory();
    final Map<Discipline, SkillState> skills = new HashMap<>();
    final Set<AugmentationProto> knownAugmentations = new HashSet<>();
    final Map<Agent, Float> personalRelations = new HashMap<>();
    final Map<DamageType, Float> statusEffects = new EnumMap<>(DamageType.class);

    final PreparedAugmentations augmentations;
    float health;
    float energy;
    int level;

    float energyOffset = 0;

    int activeDefense = 0;

    public AgentInfo(Agent agent, NonPlayerActor params) {
        this(agent, params.getParams(), params.getUnique());
    }

    public AgentInfo(Agent agent, ActorParams params, boolean unique) {
        this.id = params.getId();
        this.name = params.getName();
        this.species = Species.from(params.getSpecies());
        this.unique = unique;

        augmentations = new PreparedAugmentations(agent);
        profession = Profession.fromProto(params.getProfession());
        factions = new FactionManager(agent);

        for (FactionStatus status : params.getFactionStatusList()) {
            factions.addFaction(status);
        }
        for (InventoryItem item : params.getInventoryItemList()) {
            inventory.add(item);
        }
        for (Skill skill : params.getSkillList()) {
            skills.put(skill.getDiscipline(), new SkillState(skill));
        }
        for (AugmentationProto knownAug : params.getKnownAugIdList()) {
            knownAugmentations.add(knownAug);
            Augmentation aug = Augmentation.fromProto(knownAug);
            if (aug != null) {
                addAugmentation(aug);
            }
        }

        // add default augmentations if not otherwise specified
        if (knownAugmentations.isEmpty()) {
            for (Augmentation aug : profession.getStartingAugmentations()) {
                addAugmentation(aug);
            }
        }

        // post init basic state
        this.level = params.getLevel();
        health = getMaxHealth();
        energy = getMaxEnergy();
    }

    public AgentInfo(Agent agent, Profession profession, int level) {
        this.id = "Player";
        this.name = "Player";
        this.species = Species.from(ActorParams.Species.HUMAN);
        this.unique = true;
        augmentations = new PreparedAugmentations(agent);

        this.profession = profession;
        for (Augmentation aug : profession.getStartingAugmentations()) {
            addAugmentation(aug);
        }
        for (Skill skill : profession.getSkillsFor(level)) {
            skills.put(skill.getDiscipline(), new SkillState(skill));
        }

        // post init basic state
        this.level = level;
        health = getMaxHealth();
        energy = getMaxEnergy();

        factions = new FactionManager(agent);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Species getSpecies() {
        return species;
    }

    public Profession getProfession() {
        return profession;
    }

    public int getLevel() {
        return level;
    }

    public boolean isUnique() {
        return unique;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public FactionManager getFactionManager() {
        return factions;
    }

    public Set<Faction> getFactions() {
        return factions.getFactions();
    }

    public boolean hasRank(Faction faction) {
        return factions.getRank(faction) > 0;
    }

    public int getRank(Faction faction) {
        return factions.getRank(faction);
    }

    public void addFaction(Faction faction, int rank, int reputation) {
        factions.addFaction(faction, rank, reputation);
    }

    public int getReputation(Faction faction) {
        return factions.getReputation(faction);
    }

    public float getPersonalRelation(Agent other) {
        return Functions.forMap(personalRelations, 0f).apply(other);
    }

    public void changePersonalRelation(Agent other, float delta) {
        personalRelations.put(other, getPersonalRelation(other) + delta);
    }

    public float getDisposition(Agent other) {
        return getPersonalRelation(other) + factions.getDisposition(other);
    }

    public PreparedAugmentations getAugmentations() {
        return augmentations;
    }

    public Collection<AugmentationProto> getKnownAugmentations() {
        return knownAugmentations;
    }

    public void useAugmentation(int index) {
        augmentations.use(index);
    }

    public void addAugmentation(Augmentation aug) {
        augmentations.addAugmentation(aug);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void resetHealth() {
        setHealth(getMaxHealth());
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    /**
     * hp = warfare + level * 0.1 * warfare
     */
    public float getBaseHealth() {
        return getWarfare() + getLevel() * 0.05f * getWarfare();
    }
    
    public float getMaxHealth() {
        return getBaseHealth() * getStatusEffect(DamageType.THERMAL);
    }

    public void resetEnergy() {
        setEnergy(getMaxEnergy());
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public float getEnergy() {
        return energy;
    }

    public float getEnergyPercent() {
        return energy / getMaxEnergy();
    }

    public void changeBaseEnergy(float delta) {
        energyOffset += delta;
    }
    
    public float getBaseEnergy() {
        return 10f + getAutomata() / 2f + getLevel() * 0.01f * getAutomata();
    }

    public float getMaxEnergy() {
        return getBaseEnergy() + energyOffset;
    }

    public float expend(float value) {
        float delta = Math.max(Math.min(value, energy), 0);
        energy -= delta;
        return delta;
    }

    public float restore(float value) {
        value *= BASE_ENERGY_RATE * getStatusEffect(DamageType.VIRAL);
        float delta = Math.max(Math.min(value, getMaxEnergy() - energy), 0);
        energy += delta;
        return delta;
    }

    public float damage(float value) {
        float delta = Math.max(Math.min(value, health), 0);
        health -= delta;
        return delta;
    }

    public float heal(float value) {
        float delta = Math.max(Math.min(value, getMaxHealth() - health), 0);
        health += delta;
        return delta;
    }
    
    public void addStatus(DamageType damage, float magnitude) {
        if (!statusEffects.containsKey(damage)) {
            statusEffects.put(damage, 0f);
        }
        statusEffects.put(damage, statusEffects.get(damage) + magnitude);
    }
    
    public float getStatusEffect(DamageType damage) {
        if (!statusEffects.containsKey(damage)) {
            return 1;
        }
        
        // status effect can never more than half something's effectiveness
        float effect = Math.min(statusEffects.get(damage), 50f);
        return 1f - effect / 100f;
    }

    public int getSkillLevel(Discipline d) {
        return skills.get(d).getLevel();
    }

    public int getWarfare() {
        return skills.get(Discipline.WARFARE).getLevel();
    }

    public int getAutomata() {
        return skills.get(Discipline.AUTOMATA).getLevel();
    }

    public int getSubterfuge() {
        return skills.get(Discipline.SUBTERFUGE).getLevel();
    }

    public int getCharisma() {
        return skills.get(Discipline.CHARISMA).getLevel();
    }

    public float getOffense() {
        return 0.75f + getWarfare() / 100f;
    }

    public float getWillpower() {
        return Math.min(getAutomata() / 100f, 1.0f);
    }

    public float getDeception() {
        return Math.min(getSubterfuge() / 100f, 1.0f);
    }

    public float getDefense() {
        return Math.min(getDefenseBonus(), 1.0f);
    }

    /**
     * Returns a real value [0, 1] representing the endurance of the agent. The higher the
     * endurance, the longer the agent can act without resting. This applies primarily to the
     * wander/idle cycle of NPCs, but could be expanded to include other concepts.
     */
    public float getEndurance() {
        return Math.max(Math.min(getWarfare() / 100f - getResistanceBonus(), 1.0f), 0);
    }

    public float getResistance() {
        return Math.min(getAutomata() / 100f + getResistanceBonus(), 1.0f);
    }

    public float getPerception() {
        return Math.min(getSubterfuge() / 100f + getAlertness() + getPerceptionBonus(), 1.0f);
    }

    public float getAttackModifier() {
        return 0.5f + getWarfare() / 100f;
    }
    
    public float getExecuteModifier() {
        return 0.5f + getAutomata() / 100f;
    }

    public void modActiveDefense(int bonus) {
        activeDefense += bonus;
    }
    
    public float getDamageReduction(DamageType damage, float magnitude) {
        // cannot reduce to less than 25% of the total
        float reduction = getArmorReduction(damage, magnitude) + getResistance(damage, magnitude);
        return Math.min(reduction, 4);
    }
    
    public float getArmorReduction(DamageType damage, float magnitude) {
        float armorRating = 0;
        if (inventory.hasOutfit()) {
            Outfit outfit = inventory.getOutfit();
            armorRating += outfit.getDefense(damage);
        }
        return 1f + armorRating / magnitude;
    }
    
    public float getResistance(DamageType damage, float magnitude) {
        float rating = 0;
        switch (damage) {
            case PHYSICAL:
                // scale with warfare
                // at 100 warfare, we have 20 resistance naturally
                rating += getWarfare() / 5;
            case VIRAL:
                // scale with automata
                rating += getAutomata() / 5;
            default:
                // nothing
        }
        
        // overall level resistance
        // at level 25, we have 25 resistance naturally
        rating += getLevel();
        return rating / magnitude;
    }
    
    public float getDamageScale(DamageType damage) {
        return species.getDamageScale(damage);
    }

    public float getDefenseBonus() {
        float bonus = activeDefense / 100f;
        if (inventory.hasOutfit()) {
            Outfit outfit = inventory.getOutfit();
            bonus += outfit.getDefense();
        }
        return bonus;
    }

    public float getResistanceBonus() {
        return 0;
    }

    public float getPerceptionBonus() {
        return 0;
    }

    public float getAlertness() {
        return 0;
    }

    public float getMaxTargetDistance() {
        return 250 * getPerception();
    }

    public ActorParams serialize() {
        ActorParams.Builder builder = ActorParams.newBuilder();
        builder.setName(name);
        builder.setSpecies(species.toProto());
        builder.setProfession(profession.toProto());
        builder.setLevel(level);

        // add factions
        for (Faction faction : factions.getFactions()) {
            builder.addFactionStatus(factions.toProto(faction));
        }

        // add inventory
        for (ItemState item : inventory.getItems()) {
            builder.addInventoryItem(item.toProto());
        }

        // add skills
        for (Entry<Discipline, SkillState> skill : skills.entrySet()) {
            builder.addSkill(skill.getValue().toProto(skill.getKey()));
        }

        // add known augs
        builder.addAllKnownAugId(knownAugmentations);

        return builder.build();
    }

    /**
     * Fragments required to ascend to the given level.
     */
    public static int getFragmentRequirement(int level) {
        int fragments = 500; // base line

        // add 15 for every level up to 10
        int counted = Math.min(level, 10);
        fragments += counted * 15;

        // next add a linear increase of 10 to the previous increase for every level after 10
        int remaining = level - 10;
        if (remaining > 0) {
            // 15 * N + 10 * N * (N + 1) / 2
            // 15 + 15 + ... + 10 + 20 + 30 + ...
            int a = 15 * remaining;
            int b = (10 * remaining * (remaining + 1)) / 2;
            fragments += a + b;
        }

        return fragments;
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

        public Skill toProto(Discipline discipline) {
            return Skill.newBuilder().setDiscipline(discipline).setLevel(level).setXp(xp).build();
        }
    }
}
