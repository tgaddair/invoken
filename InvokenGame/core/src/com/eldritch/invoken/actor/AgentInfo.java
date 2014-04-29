package com.eldritch.invoken.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.factions.FactionManager;
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Items.Item;

public class AgentInfo {
	final Profession profession;
	final FactionManager factions;
	private final Map<String, ItemState> inventory = new HashMap<String, ItemState>();
	final Map<Discipline, SkillState> skills = new HashMap<Discipline, SkillState>();
	final Set<String> knownAugmentations = new HashSet<String>();
	
	final PreparedAugmentations augmentations;
	float health;
	int level;
	
	public AgentInfo(Agent agent, ActorParams params) {
		augmentations = new PreparedAugmentations(agent);
		profession = Profession.fromProto(params.getProfession());
		factions = new FactionManager(agent);
		
		for (Augmentation aug : profession.getStartingAugmentations()) {
			addAugmentation(aug);
		}
		for (FactionStatus status : params.getFactionStatusList()) {
			factions.addFaction(status);
        }
        for (InventoryItem item : params.getInventoryItemList()) {
            inventory.put(item.getItemId(), new ItemState(item));
        }
		for (Skill skill : params.getSkillList()) {
            skills.put(skill.getDiscipline(), new SkillState(skill));
        }
		for (String knownAug : params.getKnownAugIdList()) {
            knownAugmentations.add(knownAug);
        }
		
		// post init basic state
		health = getBaseHealth();
		this.level = params.getLevel();
	}
	
	public AgentInfo(Agent agent, Profession profession, int level) {
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
		this.level = level;
		
		factions = new FactionManager(agent);
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
    
    public float getDefenseBonus() {
    	return 0;
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
	
	public static class ItemState {
        private final Item item;
        private int count;

        public ItemState(Item item, int count) {
            this.item = item;
            this.count = count;
        }

        public ItemState(InventoryItem item) {
            this.item = InvokenGame.ITEM_READER.readAsset(item.getItemId());
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
}
