package com.eldritch.scifirpg.editor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.proto.Actors.ActorParams.Skill;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.proto.Disciplines.Profession;
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
			return ImmutableList.of(Discipline.SUBTERFUGE,
					Discipline.SUBTERFUGE);
		case AGENT:
			return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.CHARISMA);
		case BROKER:
			return ImmutableList.of(Discipline.CHARISMA, Discipline.CHARISMA);
		default:
			throw new IllegalArgumentException("Unrecognized profession " + p);
		}
	}

	public static List<Skill> getSorted(List<Skill> skills,
			final Collection<Discipline> masteries) {
		Collections.sort(skills, new Comparator<Skill>() {
			@Override
			public int compare(Skill s1, Skill s2) {
				return masteries.contains(s2.getDiscipline()) ? 1 : masteries
						.contains(s1.getDiscipline()) ? -1 : 0;
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

		// Assign skill points wisely.
		Map<Discipline, Double> weights = new HashMap<>();
		for (Discipline d : Discipline.values()) {
			weights.put(d, 1.0);
		}

		// In general, each master should be 50% more likely than a non-master.
		// So if we double down on a mastery, it should be twice as represented
		// as other skills.
		Collection<Discipline> masteries = getMasteriesFor(p);
		for (Discipline d : masteries) {
			weights.put(d, weights.get(d) + 0.5);
		}
		WeightedSample<Discipline> sample = new WeightedSample<>(weights);

		// Start leveling from level 2. 1 skill point per level.
		int points = level - 1;
		for (int i = 0; i < points; i++) {
			Discipline d = sample.sample();
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + 1);
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
