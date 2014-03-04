package com.eldritch.scifirpg.game.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.google.common.collect.ImmutableList;

public class ProfessionUtil {
    public static List<String> getStartingAugmentationsFor(Profession p) {
        switch (p) {
            case CENTURION:
                return ImmutableList.of("Fire", "Rend", "Sentinel");
            case EXECUTOR:
                return ImmutableList.of("Rend", "Resist", "Regenerate");
            case ASSASSIN:
                return ImmutableList.of("Stalk", "Rend", "Sentinel");
            case WARDEN:
                return ImmutableList.of("Fire", "Rend", "Sentinel"); // Suppress
            case ARCHITECT:
                return ImmutableList.of("VampiricTouch", "SummonSwarm");
            case GHOST:
                return ImmutableList.of("Reveal", "VampiricTouch", "Sneak");
            case INQUISITOR:
                return ImmutableList.of("Dominate", "VampiricTouch");
            case INFILTRATOR:
                return ImmutableList.of("Sneak", "Pilfer");
            case AGENT:
                return ImmutableList.of("Stalk", "Bluff", "Reveal");
            case BROKER:
                return ImmutableList.of("Bluff");
            default:
                throw new IllegalArgumentException("Unrecognized profession " + p);
        }
    }
    
	public static Profession getProfessionFor(Discipline d1, Discipline d2) {
		switch (d1) {
			case WARFARE:
				return getWarfareProfession(d2);
			case AUTOMATA:
				return getAutomataProfession(d2);
			case SUBTERFUGE:
				return getSubterfugeProfession(d2);
			case CHARISMA:
				return getCharismaProfession(d2);
			default:
				throw new IllegalStateException(
						String.format("Unrecognized discipline pair: (%s, %s)", d1, d2));
		}
		
	}
	
	private static Profession getWarfareProfession(Discipline d) {
		switch (d) {
			case WARFARE:
				return Profession.CENTURION;
			case AUTOMATA:
				return Profession.EXECUTOR;
			case SUBTERFUGE:
				return Profession.ASSASSIN;
			case CHARISMA:
				return Profession.WARDEN;
			default:
				throw new IllegalStateException(
						String.format("Unrecognized discipline: %s", d));
		}
	}
	
	private static Profession getAutomataProfession(Discipline d) {
		switch (d) {
			case WARFARE:
				return Profession.EXECUTOR;
			case AUTOMATA:
				return Profession.ARCHITECT;
			case SUBTERFUGE:
				return Profession.GHOST;
			case CHARISMA:
				return Profession.INQUISITOR;
			default:
				throw new IllegalStateException(
						String.format("Unrecognized discipline: %s", d));
		}
	}
	
	private static Profession getSubterfugeProfession(Discipline d) {
		switch (d) {
			case WARFARE:
				return Profession.ASSASSIN;
			case AUTOMATA:
				return Profession.GHOST;
			case SUBTERFUGE:
				return Profession.INFILTRATOR;
			case CHARISMA:
				return Profession.AGENT;
			default:
				throw new IllegalStateException(
						String.format("Unrecognized discipline: %s", d));
		}
	}
	
	private static Profession getCharismaProfession(Discipline d) {
		switch (d) {
			case WARFARE:
				return Profession.WARDEN;
			case AUTOMATA:
				return Profession.INQUISITOR;
			case SUBTERFUGE:
				return Profession.AGENT;
			case CHARISMA:
				return Profession.BROKER;
			default:
				throw new IllegalStateException(
						String.format("Unrecognized discipline: %s", d));
		}
	}
	
	public static List<Discipline> getMasteriesFor(Profession p) {
		switch (p) {
			case CENTURION:
				return ImmutableList.of(Discipline.WARFARE, Discipline.WARFARE);
			case EXECUTOR:
				return ImmutableList.of(Discipline.WARFARE, Discipline.AUTOMATA);
			case ASSASSIN:
				return ImmutableList.of(Discipline.WARFARE, Discipline.SUBTERFUGE);
			case WARDEN:
				return ImmutableList.of(Discipline.WARFARE, Discipline.CHARISMA);
			case ARCHITECT:
				return ImmutableList.of(Discipline.AUTOMATA, Discipline.AUTOMATA);
			case GHOST:
				return ImmutableList.of(Discipline.AUTOMATA, Discipline.SUBTERFUGE);
			case INQUISITOR:
				return ImmutableList.of(Discipline.AUTOMATA, Discipline.CHARISMA);
			case INFILTRATOR:
				return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.SUBTERFUGE);
			case AGENT:
				return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.CHARISMA);
			case BROKER:
				return ImmutableList.of(Discipline.CHARISMA, Discipline.CHARISMA);
			default:
				throw new IllegalArgumentException("Unrecognized profession " + p);
		}
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
	
	public static List<Skill> getStartingSkillsFor(Profession p) {
		// All skills default to 10
		Map<Discipline, Skill.Builder> skills = new LinkedHashMap<>();
		for (Discipline d : Discipline.values()) {
			skills.put(d, Skill.newBuilder().setDiscipline(d).setLevel(10));
		}
		
		// Assign 10 additional skill levels for each mastery
		Collection<Discipline> masteries = getMasteriesFor(p);
		for (Discipline d : masteries) {
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + 10);
		}
		
		List<Skill> results = new ArrayList<>();
        for (Skill.Builder b : skills.values()) {
            results.add(b.build());
        }
		return results;
	}
	
	public static List<Skill> getSkillsFor(Profession p, int level) {
		// All skills default to 10
		Map<Discipline, Skill.Builder> skills = new LinkedHashMap<>();
		for (Discipline d : Discipline.values()) {
			skills.put(d, Skill.newBuilder().setDiscipline(d).setLevel(10));
		}
		
		// Assign 10 additional skill levels for each mastery
		Collection<Discipline> masteries = getMasteriesFor(p);
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
		List<Skill.Builder> list = new ArrayList<>(skills.values()); 
		Random rand = new Random();
		int remaining = points - used;
		while (remaining > 0) {
			int i = rand.nextInt(4);
			Skill.Builder s = list.get(i);
			s.setLevel(s.getLevel() + 1);
			remaining--;
		}
		
		// Construct the skills
		List<Skill> results = new ArrayList<>();
		for (Skill.Builder b : skills.values()) {
			results.add(b.build());
		}
		return getSorted(results, masteries);
	}

	private ProfessionUtil() {
	}
}
