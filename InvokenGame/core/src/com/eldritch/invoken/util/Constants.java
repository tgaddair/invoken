package com.eldritch.invoken.util;

public class Constants {
    public static final String STATICS = "statics";
    public static final String DYNAMICS = "dynamics";
    public static final String COLLISION = "collision";
    public static final String CONSTRAINTS = "constraints";
    public static final String LIGHTS = "lights";
    public static final String TRANSIENT = "transient";
    public static final String BLANK = "blank";
    public static final String BUFFER = "buffer";
    public static final String Z_OFF = "z";
    public static final String RADIUS = "radius";
    public static final String ACTIVATOR = "activator";
    
    public static final String PLAYER_LAYER = "player";
    public static final String FROM_ABOVE_LAYER = "player-above";
    public static final String FROM_BELOW_LAYER = "player-below";
    
    // opposite of a traditional collision constraint, this tile explicitly allows layers named
    // "surface" to be placed here
    // dock: layer defining where something can be placed
    // anchor: layer defining tiles that must match with a dock layer in order to be placed
    public static final String DOCK = "dock";
    public static final String ANCHOR = "anchor";
    
    public static final String CATEGORY = "category";
    public static final String LOW = "low";
    
    // IDs
    public static final String CRAWLER = "Crawler";
    public static final String DUMMY_NPC = "Dummy";
    
    // Factions
    public static final String STATION_FACTION = "TheStation";
    
    private Constants() {};
}
