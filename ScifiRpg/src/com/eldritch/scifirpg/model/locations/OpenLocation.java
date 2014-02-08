package com.eldritch.scifirpg.model.locations;

import java.io.IOException;

import com.google.gson.Gson;


public class OpenLocation extends Location {
	private String parent;
	private transient LocationContainer lc;
	
	public OpenLocation() {
		lc = null;
	}
	
	public OpenLocation(String name, int actions) {
		this(name, actions, null);
	}

	public OpenLocation(String name, int actions, String parent) {
		super(name, actions);
		this.parent = parent;
		lc = null;
	}

	@Override
	public void load() throws IOException {
		Gson gson = new Gson();
		String json = readFile();
		lc = gson.fromJson(json, LocationContainer.class);
	}
	
	public static class LocationContainer {
		private Location[][] children;
		
		public LocationContainer() {
		}
	}
}
