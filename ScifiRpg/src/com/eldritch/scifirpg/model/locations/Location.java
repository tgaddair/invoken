package com.eldritch.scifirpg.model.locations;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class Location {
	private String name;
	private final Optional<Location> parent;
	
	public Location(String name) {
		this(name, Optional.<Location>absent());
	}
	
	public Location(String name, Location parent) {
		this(name, Optional.of(parent));
	}

	private Location(String name, Optional<Location> parent) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}
	
	public boolean hasParent() {
		return parent.isPresent();
	}
	
	public Location getParent() {
		return parent.get();
	}
	
	public void enter() {
		
	}
	
	public void exit() {
	}

	public void load() throws IOException {
	}

	protected String readFile() throws IOException {
		File file = new File(getFilename());
		StringBuilder fileContents = new StringBuilder((int) file.length());
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");

		try {
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}

	protected String getFilename() {
		String filename = name.replaceAll(" ", "_");
		return filename + ".json";
	}

	public String getShortDescription() {
		String desc = name;
		return desc;
	}
}
