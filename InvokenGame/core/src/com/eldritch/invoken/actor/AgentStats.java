package com.eldritch.invoken.actor;

import java.util.HashMap;
import java.util.Map;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;

public class AgentStats {
	private final Agent agent;
	final Profession profession;
	final Map<Discipline, SkillState> skills = new HashMap<Discipline, SkillState>();
	
	final PreparedAugmentations augmentations;
	float health;
	int level;
	
	public AgentStats(Agent agent, Profession profession, int level) {
		this.agent = agent;
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
}
