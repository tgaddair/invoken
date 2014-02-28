package com.eldritch.scifirpg.game.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.PlayerActor;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StateMarker;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Missions.Mission;

public class Player extends Actor {
	// Player specific parameters
    private final Set<StateMarker> markers = new HashSet<>();
    private final Set<String> npcsMet = new HashSet<>();
	private final Set<Mission> missions = new HashSet<>();
	
	private Player(PlayerActor player) {
	    this(player, true);
	}
	
	private Player(PlayerActor player, boolean setHealth) {
	    super(player.getParams());
	    
	    if (setHealth) {
	        setHealth(player.getHealth());
	    }
	    for (String itemId : player.getEquippedItemIdList()) {
	        equip(itemId);
	    }
	    for (StagedAugmentation aug : player.getStagedAugmentationList()) {
            stage(aug);
        }
	    
	    for (StateMarker marker : player.getStateMarkerList()) {
            markers.add(marker);
        }
	    for (String actorId : player.getKnownNpcList()) {
	        npcsMet.add(actorId);
        }
	    for (Mission mission : player.getMissionList()) {
	        missions.add(mission);
	    }
	}
	
	public Species getSpecies() {
		return Species.HUMAN;
	}

    public Set<Mission> getMissions() {
        return missions;
    }

    public static Player newPlayer(String name, Profession p, Gender g) {
        ActorParams params = ActorParams.newBuilder()
                .setId(WordUtils.capitalizeFully(name).replaceAll(" ", ""))
                .setName(name)
                .setProfession(p)
                .setGender(g)
                .setSpecies(Species.HUMAN)
                .setLevel(1)
                .addAllSkill(ProfessionUtil.getStartingSkillsFor(p))
                //getStartingEquipmentFor(p)
                //getStartingAugmentationsFor(p)
                .build();
        PlayerActor player = PlayerActor.newBuilder()
                .setParams(params)
                .build();
        
		return new Player(player, false);
	}
	
	public static Player fromProto(PlayerActor player) {
	    return new Player(player);
	}
}
