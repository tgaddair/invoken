package com.eldritch.scifirpg.game.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Missions.Mission;

public class PlayerState {
	// Immutable actor fields
	private final String id;
	private final String name;
	private final Profession profession;
	private final Gender gender;
	
	// Mutable actor fields
	private int level;
	private final Map<Discipline, Skill.Builder> skills;
	private final Map<String, FactionStatus.Builder> factions = new HashMap<>();
	private final Set<InventoryItem> inventory = new HashSet<>();
	private final Set<String> knownAugmentations = new HashSet<>();
	
	// Player specific parameters
	private int health;
	private final Set<InventoryItem.Builder> equipped = new HashSet<>();
	private final Set<String> stagedAugmentations = new HashSet<>();
	private final Set<Mission> missions = new HashSet<>();
	
	private PlayerState(String name, Profession p, Gender g) {
		this.id = WordUtils.capitalizeFully(name).replaceAll(" ", "");
		this.name = name;
		this.profession = p;
		this.gender = g;
		
		level = 1;
		skills = ProfessionUtil.getStartingSkillsFor(p);
		
		health = getBaseHealth();
	}
	
	public Species getSpecies() {
		return Species.HUMAN;
	}
	
	public int getBaseHealth() {
		return skills.get(Discipline.WARFARE).getLevel();
	}
	
	public static PlayerState newPlayer(String name, Profession p, Gender g) {
		return new PlayerState(name, p, g);
	}
}
