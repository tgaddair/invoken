package com.eldritch.invoken.util;

public class Settings {
	public static final int PX = 32;
	public static final float SCALE = 1.0f / PX;
	public static final float MIN_ZOOM = 1.0f;
	public static final float MAX_ZOOM = 10.0f; // for debug purposes; 2 or 1.5 is more reasonable
	
    // the fixed viewport dimensions (ratio: 1.6)
    public static final int GAME_VIEWPORT_WIDTH = 400;
    public static final int GAME_VIEWPORT_HEIGHT = 240;
    public static final int MENU_VIEWPORT_WIDTH = 1200;  // 800
    public static final int MENU_VIEWPORT_HEIGHT = 720;  // 480
	
	// collision bit filters
	public static final short BIT_DEFAULT = 0x0001;
	public static final short BIT_PHYSICAL = 0x0002;
	public static final short BIT_ANYTHING = BIT_DEFAULT | BIT_PHYSICAL;
	
	// debug settings
	public static final boolean GOD_MODE = true;
	public static final boolean DEBUG_DRAW = false;
	public static final boolean DEBUG_LIGHTS = false;
	public static final boolean DEBUG_COVER = true;
	public static final boolean SKIP_MENU = true;
	
	public static DebugGraph DRAW_GRAPH = DebugGraph.None;
	public enum DebugGraph {
	    None, Disposition, Enemies, LOS, Visible, Cover
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
