package com.eldritch.scifirpg.editor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eldritch.scifirpg.editor.tables.MajorAssetTable;

public class StateMarkers {
	public static List<String> getMarkers() {
		List<String> markers = new ArrayList<>();
		File file = new File(MajorAssetTable.getTopAssetDirectory() + "/markers.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	markers.add(line.trim());
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return markers;
	}
	
	private StateMarkers() {}
}
