package com.eldritch.invoken.actor;

import java.util.Collection;
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
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Augmentations.AugmentationProto;
import com.eldritch.invoken.proto.Disciplines.Discipline;
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
	final Map<Discipline, SkillState> skills = new HashMap<Discipline, SkillState>();
	final Set<AugmentationProto> knownAugmentations = new HashSet<AugmentationProto>();
	final Map<Agent, Float> personalRelations = new HashMap<Agent, Float>();
	
	final PreparedAugmentations augmentations;
	float health;
	float energy;
	int level;
	
	float energyOffset = 0;
	
	int activeDefense = 0;
	
	public ActorParams serialize() {
	    ActorParams.Builder builder = ActorParams.newBuilder();
	    builder.setName(name);
	    builder.setSpecies(species);
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
	
	public AgentInfo(Agent agent, ActorParams params, boolean unique) {
	    this.id = params.getId();
	    this.name = params.getName();
	    this.species = params.getSpecies();
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
		health = getBaseHealth();
		energy = getBaseEnergy();
		this.level = params.getLevel();
	}
	
	public AgentInfo(Agent agent, Profession profession, int level) {
	    this.id = "Player";
	    this.name = "Player";
	    this.species = Species.HUMAN;
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
		health = getBaseHealth();
		energy = getBaseEnergy();
		this.level = level;
		
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
		setHealth(getBaseHealth());
	}
	
	public void setHealth(float health) {
		this.health = health;
	}
	
	public float getHealth() {
		return health;
	}
	
	public float getBaseHealth() {
		return getWarfare();
	}
	
	public void resetEnergy() {
        setEnergy(getBaseEnergy());
    }
    
    public void setEnergy(float energy) {
        this.energy = energy;
    }
    
    public float getEnergy() {
        return energy;
    }
    
    public float getEnergyPercent() {
        return energy / getBaseEnergy();
    }
    
    public void changeBaseEnergy(float delta) {
    	energyOffset += delta;
    }
	
	public float getBaseEnergy() {
        return 10 + getAutomata() / 2 + energyOffset;
    }
	
	public float expend(float value) {
	    float delta = Math.max(Math.min(value, energy), 0);
	    energy -= delta;
        return delta;
	}
	
	public float restore(float value) {
	    value *= BASE_ENERGY_RATE;
        float delta = Math.max(Math.min(value, getBaseEnergy() - energy), 0);
        energy += delta;
        return delta;
    }
	
	public float damage(float value) {
		float delta = Math.max(Math.min(value, health), 0);
		health -= delta;
		return delta;
	}
	
	public float heal(float value) {
		float delta = Math.max(Math.min(value, getBaseHealth() - health), 0);
		health += delta;
		return delta;
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
    
    public float getAccuracy() {
    	return 0.75f + getWarfare() / 100f;
    }
    
    public float getWillpower() {
    	return 0.5f + getAutomata() / 100f;
    }
    
    public float getDeception() {
    	return 0.5f + getSubterfuge() / 100f;
    }
    
    public float getDefense() {
    	return Math.min(getDefenseBonus(), 1.0f);
    }
    
    /**
     * Returns a real value [0, 1] representing the endurance of the agent.  The higher the
     * endurance, the longer the agent can act without resting.  This applies primarily to the
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
    
    public void modActiveDefense(int bonus) {
        activeDefense += bonus;
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
	
	/**
	 * Fragments required to ascend to the given level.
	 */
	public static int getFragmentRequirement(int level) {
	    int fragments = 500;  // base line
	    
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
