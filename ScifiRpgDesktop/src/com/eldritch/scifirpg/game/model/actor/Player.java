package com.eldritch.scifirpg.game.model.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.ActorParams.Gender;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.invoken.proto.Actors.PlayerActor.StateMarker;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.proto.Disciplines.Profession;
import com.eldritch.invoken.proto.Missions.Mission;

public class Player extends Actor {
	// Player specific parameters
    private final Map<String, StateMarker> markers = new HashMap<>();
    private final Set<String> npcsMet = new HashSet<>();
	private final Map<String, Mission> missions = new HashMap<>();
	
	private Player(PlayerActor player) {
	    super(player.getParams());
	    
	    for (String itemId : player.getEquippedItemIdList()) {
	        equip(itemId);
	    }
	    for (StagedAugmentation aug : player.getStagedAugmentationList()) {
            stage(aug);
        }
	    
	    for (StateMarker marker : player.getStateMarkerList()) {
            markers.put(marker.getName(), marker);
        }
	    for (String actorId : player.getKnownNpcList()) {
	        npcsMet.add(actorId);
        }
	    for (Mission mission : player.getMissionList()) {
	        missions.put(mission.getId(), mission);
	    }
	}
	
	public void gainExperience(Discipline d, int xp) {
	    // TODO handle leveling up, maybe only on rest
	    getSkill(d).addXp(xp);
	}
	
	public int getMarkerCount(String marker) {
	    if (!markers.containsKey(marker)) {
	        return 0;
	    }
	    return markers.get(marker).getCount();
	}
	
	public Species getSpecies() {
		return Species.HUMAN;
	}
	
	public boolean hasFollower(String actorId) {
	    // TODO
	    return false;
	}
	
	public int getMissionStage(String missionId) {
	    // TODO
	    //return missions.get(missionId).getStage();
	    return 0;
	}

    public Collection<Mission> getMissions() {
        return missions.values();
    }
    
    @Override
    public void takeCombatTurn(ActionModel model) {
        // Handled by user interface
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
        
		return new Player(player);
	}
	
	public static Player fromProto(PlayerActor player) {
	    return new Player(player);
	}
}
