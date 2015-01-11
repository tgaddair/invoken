package com.eldritch.invoken.util;

public class Settings {
	public static final int PX = 32;
	public static final float SCALE = 1 / 32f;
	public static final int MAX_WIDTH = 100;
	public static final int MAX_HEIGHT = 100;
	public static final float MIN_ZOOM = 1.0f;
	public static final float MAX_ZOOM = 10.0f; // for debug purposes; 2 or 1.5 is more reasonable
	
	// collision bit filters
	public static final short BIT_DEFAULT = 0x0001;
	public static final short BIT_PHYSICAL = 0x0002;
	public static final short BIT_ANYTHING = BIT_DEFAULT | BIT_PHYSICAL;
	
	// debug settings
	public static final boolean GOD_MODE = true;
	public static boolean DEBUG_DRAW = false;
	public static boolean DRAW_DISPOSITION = true;
	public static boolean DRAW_LOS = false;

	private Settings() {}
}
