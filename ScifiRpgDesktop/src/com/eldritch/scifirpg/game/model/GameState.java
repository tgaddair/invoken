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
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.PlayerActor;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Disciplines.Profession;

public class GameState {
	private final LocationModel locationModel;
	private final ActorModel actorModel;
	private final Map<String, Set<String>> knownEncounters = new HashMap<>();
	
	public GameState(Profession p) {
	    //List<InventoryItem> inventory;
	    List<StagedAugmentation> augs = new ArrayList<>();
	    augs.add(StagedAugmentation.newBuilder()
	            .setAugId("Fire").setRemainingUses(20).setStages(20).build());
	    
		ActorParams params = ActorParams.newBuilder()
                .setId("Tester")
                .setName("Tester")
                .setProfession(p)
                .setGender(Gender.MALE)
                .setSpecies(Species.HUMAN)
                .setLevel(10)
                .addAllSkill(ProfessionUtil.getSkillsFor(p, 10))
                //getStartingEquipmentFor(p)
                //getStartingAugmentationsFor(p)
                .build();
        PlayerActor proto = PlayerActor.newBuilder()
                .setParams(params)
                .addAllStagedAugmentation(augs)
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
