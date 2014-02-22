package com.eldritch.scifirpg.editor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.eldritch.scifirpg.proto.Actors.ActorParams.Profession;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.google.common.collect.ImmutableList;

public class ProfessionUtil {
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
		System.out.println("points: " + points);
		
		// Divide the first 1/3 of the points into a separate pool for masteries.
		// Spread them uniformly.
		int available = points / 3;
		System.out.println("available: " + available);
		
		int used = 0;
		for (Discipline d : masteries) {
			int a = available / 2;
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + a);
			used += a;
		}
		System.out.println("used: " + used);
		
		// Now we want to take the remainder and distribute them randomly
		List<Skill.Builder> list = new ArrayList<>(skills.values()); 
		Random rand = new Random();
		int remaining = points - used;
		System.out.println("remaining: " + remaining);
		while (remaining > 0) {
			int i = rand.nextInt(4);
			Skill.Builder s = list.get(i);
			s.setLevel(s.getLevel() + 1);
			remaining--;
		}
		
		// Construct the skills
		List<Skill> result = new ArrayList<>();
		for (Skill.Builder b : skills.values()) {
			result.add(b.build());
		}
		return getSorted(result, masteries);
	}

	private ProfessionUtil() {
	}
}
