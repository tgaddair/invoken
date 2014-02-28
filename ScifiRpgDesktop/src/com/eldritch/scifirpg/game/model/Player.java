package com.eldritch.scifirpg.game.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.PlayerActor;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Missions.Mission;

public class Player extends Actor {
	// Player specific parameters
	private final Set<Mission> missions = new HashSet<>();
	
	private Player(PlayerActor player) {
	    super(player.getParams());
	    
	    for (String itemId : player.getEquippedItemIdList()) {
	        equip(itemId);
	    }
	    for (StagedAugmentation aug : player.getStagedAugmentationList()) {
            stage(aug);
        }
	    for (Mission mission : player.getMissionList()) {
	        missions.add(mission);
	    }
	}
	
	private Player(String name, Profession p, Gender g) {
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

    public Set<Mission> getMissions() {
        return missions;
    }

    public static Player newPlayer(String name, Profession p, Gender g) {
		return new Player(name, p, g);
	}
	
	public static Player fromProto(PlayerActor player) {
	    return new Player(player);
	}
}
