package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Barrier;
import com.eldritch.invoken.actor.aug.Cloak;
import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.aug.Frenzy;
import com.eldritch.invoken.actor.aug.Drain;
import com.eldritch.invoken.actor.aug.FireWeapon;
import com.eldritch.invoken.actor.aug.Jaunt;
import com.eldritch.invoken.actor.aug.Mirror;
import com.eldritch.invoken.actor.aug.Paralyze;
import com.eldritch.invoken.actor.aug.RendWeapon;
import com.eldritch.invoken.actor.aug.Resurrect;
import com.eldritch.invoken.actor.aug.Scramble;
import com.eldritch.invoken.proto.Actors.ActorParams.Skill;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.google.common.collect.ImmutableList;

public abstract class Profession {
    public static Profession getDefault() {
        return new Executor();
    }
    
	public static List<Skill> getSorted(List<Skill> skills, final Collection<Discipline> masteries) {
		Collections.sort(skills, new Comparator<Skill>() {
			@Override
			public int compare(Skill s1, Skill s2) {
				return masteries.contains(s2.getDiscipline()) ? 1 :
					masteries.contains(s1.getDiscipline()) ? -1 : 0;
			}
		});
		return skills;
	}
	
	public List<Skill> getSkillsFor(int level) {
		// All skills default to 10
		Map<Discipline, Skill.Builder> skills = new LinkedHashMap<Discipline, Skill.Builder>();
		for (Discipline d : Discipline.values()) {
			skills.put(d, Skill.newBuilder().setDiscipline(d).setLevel(10));
		}
		
		// Assign 10 additional skill levels for each mastery
		Collection<Discipline> masteries = getMasteries();
		for (Discipline d : masteries) {
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + 10);
		}
		
		// Start leveling from level 2.  5 skill points per level.  Assign skill points
		// intelligently.
		// In general, we want to maintain a 2:2:1:1 ratio.
		// For level n, we have 5(n-1) skill points to distribute
		int points = 5 * (level - 1);
		
		// Divide the first 1/3 of the points into a separate pool for masteries.
		// Spread them uniformly.
		int available = points / 3;
		
		int used = 0;
		for (Discipline d : masteries) {
			int a = available / 2;
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + a);
			used += a;
		}
		
		// Now we want to take the remainder and distribute them randomly
		List<Skill.Builder> list = new ArrayList<Skill.Builder>(skills.values()); 
		Random rand = new Random();
		int remaining = points - used;
		while (remaining > 0) {
			int i = rand.nextInt(4);
			Skill.Builder s = list.get(i);
			s.setLevel(s.getLevel() + 1);
			remaining--;
		}
		
		// Construct the skills
		List<Skill> result = new ArrayList<Skill>();
		for (Skill.Builder b : skills.values()) {
			result.add(b.build());
		}
		return getSorted(result, masteries);
	}
	
	public abstract List<Discipline> getMasteries();
	
	public abstract List<Augmentation> getStartingAugmentations();
	
	public static class Centurion extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.WARFARE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon(), new Barrier());
		}
	}
	
	public static class Executor extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.AUTOMATA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon(), new Jaunt(), new Drain(), new Mirror());
		}
	}
	
	public static class Assassin extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.SUBTERFUGE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new RendWeapon(), new FireWeapon());
		}
	}
	
	public static class Warden extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Architect extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.AUTOMATA, Discipline.AUTOMATA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new Drain(), new Resurrect());
		}
	}
	
	public static class Ghost extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.AUTOMATA, Discipline.SUBTERFUGE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon(), new Cloak(), new Crack(), new Scramble());
		}
	}
	
	public static class Inquisitor extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.AUTOMATA, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new Drain(), new Paralyze(), new Frenzy());
		}
	}
	
	public static class Infiltrator extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.SUBTERFUGE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Agent extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Broker extends Profession {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.CHARISMA, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static Profession fromProto(com.eldritch.invoken.proto.Disciplines.Profession p) {
		switch (p) {
			case CENTURION:
				return new Centurion();
			case EXECUTOR:
				return new Executor();
			case ASSASSIN:
				return new Assassin();
			case WARDEN:
				return new Warden();
			case ARCHITECT:
				return new Architect();
			case GHOST:
				return new Ghost();
			case INQUISITOR:
				return new Inquisitor();
			case INFILTRATOR:
				return new Infiltrator();
			case AGENT:
				return new Agent();
			case BROKER:
				return new Broker();
			default:
				throw new IllegalArgumentException("Unrecognized Profession: " + p);
		}
	}
}
