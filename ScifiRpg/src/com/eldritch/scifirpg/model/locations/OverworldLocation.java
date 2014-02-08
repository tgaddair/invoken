package com.eldritch.scifirpg.model.locations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;


public class OverworldLocation extends Location {
	private transient LocationContainer lc;

	public OverworldLocation(String name, Location parent, LocationContainer lc) {
		super(name, parent);
		this.lc = lc;
	}

	@Override
	public void load() throws IOException {
		Gson gson = new Gson();
		String json = readFile();
		lc = gson.fromJson(json, LocationContainer.class);
	}
	
	public static class LocationContainer {
		private List<List<Location>> children;
		
		public LocationContainer(List<List<Location>> children) {
			this.children = children;
		}
	}
	
	public static class Builder {
		private String name;
		private Location parent;
		private List<List<Location>> children;
		
		private Builder() {
			children = new ArrayList<List<Location>>();
			addGridRow();
		}
		
		public final void addGridRow() {
			children.add(new ArrayList<Location>());
		}
		
		public void addGridLocation(Location location) {
			children.get(children.size() - 1).add(location);
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setParent(Location parent) {
			this.parent = parent;
		}
		
		public OverworldLocation build() {
			return new OverworldLocation(name, parent, new LocationContainer(children));
		}
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
}
