package com.eldritch.invoken.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.InvokenGame;
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
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.google.common.base.Functions;

public class AgentInfo {
    final String name;
    final Species species;
    
	final Profession profession;
	final FactionManager factions;
	private final Inventory inventory = new Inventory();
	final Map<Discipline, SkillState> skills = new HashMap<Discipline, SkillState>();
	final Set<String> knownAugmentations = new HashSet<String>();
	final Map<Agent, Float> personalRelations = new HashMap<Agent, Float>();
	
	final PreparedAugmentations augmentations;
	float health;
	float energy;
	int level;
	
	float energyOffset = 0;
	
	int activeDefense = 0;
	
	public AgentInfo(Agent agent, ActorParams params) {
	    this.name = params.getName();
	    this.species = params.getSpecies();
	    InvokenGame.log("creating: " + name);
	    
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
		for (String knownAug : params.getKnownAugIdList()) {
            knownAugmentations.add(knownAug);
            Augmentation aug = Augmentation.fromProto(InvokenGame.AUG_READER.readAsset(knownAug));
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
	    this.name = "Player";
	    this.species = Species.HUMAN;
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
	
	public String getName() {
	    return name;
	}
	
	public Species getSpecies() {
		return species;
	}
	
	public int getLevel() {
	    return level;
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
	
	public Collection<String> getKnownAugmentations() {
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
    
    public void changeBaseEnergy(float delta) {
    	energyOffset += delta;
    }
	
	public float getBaseEnergy() {
        return getAutomata() + energyOffset;
    }
	
	public float expend(float value) {
	    float delta = Math.max(Math.min(value, energy), 0);
	    energy -= delta;
        return delta;
	}
	
	public float restore(float value) {
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
}
