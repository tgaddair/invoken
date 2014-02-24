package com.eldritch.scifirpg.editor.util;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class StateMarkers {
	public static List<String> getMarkers() {
		return ImmutableList.<String>of(
				"AssassinateVeraZan",
				"KilledGothWithVera"
				);
	}
	
	private StateMarkers() {}
}
