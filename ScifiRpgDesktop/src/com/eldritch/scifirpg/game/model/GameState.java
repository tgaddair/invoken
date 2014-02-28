package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.game.util.ProfessionUtil;
import com.eldritch.scifirpg.proto.Actors.ActorParams;
import com.eldritch.scifirpg.proto.Actors.ActorParams.InventoryItem;
import com.eldritch.scifirpg.proto.Actors.PlayerActor;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Disciplines.Profession;

public class GameState {
	private final LocationModel locationModel;
	private final ActorModel actorModel;
	
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
		locationModel = new LocationModel("IlithExterior");
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
