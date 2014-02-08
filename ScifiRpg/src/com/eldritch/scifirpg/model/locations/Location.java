package com.eldritch.scifirpg.model.locations;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Location {
	private String name;
	private int actions;
	
	public Location() {
	}

	public Location(String name, int actions) {
		this.name = name;
		this.actions = actions;
	}

	public String getName() {
		return name;
	}

	public int getActionCost() {
		return actions;
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
		if (actions > 0) {
			desc += String.format(" (%d)", actions);
		}
		return desc;
	}
}
