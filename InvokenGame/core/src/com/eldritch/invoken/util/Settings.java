package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.Profession;

public class Settings {
	public static final int PX = 32;
	public static final float SCALE = 1.0f / PX;
	public static final float MIN_ZOOM = 1.0f;
	public static final float MAX_ZOOM = 10.0f; // for debug purposes; 2 or 1.5 is more reasonable
	public static final float DEFAULT_ZOOM = 1.15f;
//	public static final float DEFAULT_ZOOM = 1f;
	public static final float FRAME_DURATION = 0.15f;
	
	// audio
	public static final boolean MUTE = false;
//	public static final float MUSIC_VOLUME = 0.25f;
	public static final float MUSIC_VOLUME = 0f;
	public static final float SFX_VOLUME = 0.1f;
	
	// a number just greater than the previous for the purpose of approximating a total ordering
	// real numbers
	public static final float EPSILON = 1e-5f;
	
    // the fixed viewport dimensions (ratio: 1.6)
    public static final int GAME_VIEWPORT_WIDTH = 400;
    public static final int GAME_VIEWPORT_HEIGHT = 240;
    public static final int MENU_VIEWPORT_WIDTH = 1200;  // 800
    public static final int MENU_VIEWPORT_HEIGHT = 720;  // 480
	
	// collision bit filters
	public static final short BIT_NOTHING = 0x0000;
	public static final short BIT_DEFAULT = 0x0001;
	public static final short BIT_AGENT = 0x0002;
	public static final short BIT_WALL = 0x0004;
	public static final short BIT_OBSTACLE = 0x0008;
	public static final short BIT_SHORT_OBSTACLE = 0x0010;
	public static final short BIT_BULLET = 0x0020;
	public static final short BIT_LOW_AGENT = 0x0040;
	public static final short BIT_PERIMETER = 0x0080;
	public static final short BIT_STATIC = BIT_WALL | BIT_OBSTACLE | BIT_SHORT_OBSTACLE;
	public static final short BIT_HIGH_SHOOTABLE = BIT_AGENT | BIT_WALL | BIT_OBSTACLE;
	public static final short BIT_SHOOTABLE = BIT_HIGH_SHOOTABLE | BIT_LOW_AGENT;
	public static final short BIT_PHYSICAL = BIT_SHOOTABLE | BIT_SHORT_OBSTACLE;  // can walk into
	public static final short BIT_ANYTHING = BIT_DEFAULT | BIT_PHYSICAL | BIT_BULLET;  // everything
	
	// visuals
	public static final boolean ENABLE_FOG = false;
	
	// debug settings
	public static final boolean GOD_MODE = false;
	public static final boolean DEBUG_DRAW = false;
	public static final boolean DEBUG_LIGHTS = false;
	public static final boolean DEBUG_COVER = false;
	public static final boolean DEBUG_PATHFINDING = false;
	public static final boolean DEBUG_STEALTH = false;
	public static final boolean DEBUG_MAP = true;
	public static final boolean SKIP_MENU = true;
	public static final int START_LEVEL = 10;  // 25
	public static final Profession DEFAULT_PROFESSION = Profession.Ghost;
	
//	public static final String FIRST_LOCATION = "Tutorial";
	public static final String FIRST_LOCATION = "WelcomeCenter";
//	public static final String FIRST_LOCATION = "WelcomeCenterLevel3";
	
	// tutorial seed:
	// global seed: -509684375407364354
	// hash code: 257920894
	// seed: -509684375158687360
	
	public static DebugGraph DRAW_GRAPH = DebugGraph.Disposition;
	public enum DebugGraph {
	    None, Disposition, Enemies, LOS, Visible
	}
	
	public static void lastDebugGraph() {
        int current = DRAW_GRAPH.ordinal();
        int next = current - 1;
        if (next < 0) {
            next = DebugGraph.values().length - 1;
        }
        DRAW_GRAPH = DebugGraph.values()[next];
    }
	
	public static void nextDebugGraph() {
	    int current = DRAW_GRAPH.ordinal();
	    int next = (current + 1) % DebugGraph.values().length;
	    DRAW_GRAPH = DebugGraph.values()[next];
	}

	private Settings() {}
}
