package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Disciplines.Profession;

public class GameState {
	private final LocationModel locationModel;
	private final ActorModel actorModel;
	
	public GameState(Profession p) {
		String name = "Tester";
		Gender g = Gender.MALE;
		Player player = Player.newPlayer(name, p, g);
		
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
