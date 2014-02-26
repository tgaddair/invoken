package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.game.util.LocationMarshaller;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Disciplines.Profession;
import com.eldritch.scifirpg.proto.Locations.Location;

public class GameState {
	private final PlayerState player;
	private final LocationMarshaller locationMarshaller = new LocationMarshaller();
	
	private Location location;
	
	public GameState(Profession p) {
		String name = "Tester";
		Gender g = Gender.MALE;
		player = PlayerState.newPlayer(name, p, g);
		
		location = locationMarshaller.readAsset("IlithExterior");
	}
	
	public LocationModel getLocation() {
		return new LocationModel(location);
	}
}
