package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eldritch.scifirpg.game.model.actor.ActorModel;
import com.eldritch.scifirpg.game.model.actor.Player;
import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Actors.ActorParams.Gender;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.invoken.proto.Disciplines.Profession;

public class GameState {
	private final LocationModel locationModel;
	private final ActorModel actorModel;
	private final Map<String, Set<String>> knownEncounters = new HashMap<>();
	
	public GameState(Profession p) {
	    //List<InventoryItem> inventory;
	    
	    List<StagedAugmentation> staged = new ArrayList<>();
	    List<String> startingAugs = ProfessionUtil.getStartingAugmentationsFor(p);
	    for (String augid : startingAugs) {
	        staged.add(StagedAugmentation.newBuilder()
	                .setAugId(augid).setRemainingUses(10).setStages(10).build());
	    }
	    
		ActorParams params = ActorParams.newBuilder()
                .setId("Tester")
                .setName("Tester")
                .setProfession(p)
                .setGender(Gender.MALE)
                .setSpecies(Species.HUMAN)
                .setLevel(10)
                .addAllSkill(ProfessionUtil.getSkillsFor(p, 10))
                //getStartingEquipmentFor(p)
                .addAllKnownAugId(startingAugs)
                .build();
        PlayerActor proto = PlayerActor.newBuilder()
                .setParams(params)
                .addAllStagedAugmentation(staged)
                .build();
		Player player = Player.fromProto(proto);
		
		actorModel = new ActorModel(player);
		locationModel = new LocationModel("IlithExterior", this);
		
		// TODO load known encounters from the saved Player game state
	}
	
	public boolean encounterKnown(String locid, String encid) {
	    if (!knownEncounters.containsKey(locid)) {
            return false;
        }
        return knownEncounters.get(locid).contains(encid);
	}
	
	public void addKnownEncounter(String locid, String encid) {
	    if (!knownEncounters.containsKey(locid)) {
	        knownEncounters.put(locid, new HashSet<String>());
	    }
	    knownEncounters.get(locid).add(encid);
	}
	
	public void setLocation(String locid) {
	    locationModel.setCurrent(locid);
	}
	
	public LocationModel getLocationModel() {
		return locationModel;
	}
	
	public ActorModel getActorModel() {
	    return actorModel;
	}
}
