package com.eldritch.scifirpg.model;

public class GameSettings {
	private static GameSettings instance = null;
	private final Person player;
	
	private GameSettings(Person player) {
		this.player = player;
	}
	
	public Person getPlayer() {
		return player;
	}
	
	public static GameSettings newGame(Person player) {
		instance = new GameSettings(player);
		return instance;
	}
	
	public static GameSettings getGame() {
		return instance;
	}
}
