package com.eldritch.invoken.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.util.Settings;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		// create the listener that will receive the application events
		ApplicationListener listener = new InvokenGame();
		
		// define the window's title
		config.title = "Invoken";
		
		// define the window's size
		config.width = Settings.MENU_VIEWPORT_WIDTH;
		config.height = Settings.MENU_VIEWPORT_HEIGHT;
		
		// whether to use OpenGL ES 2.0
		// causes a blank screen when using v1.4.1 and v1.5.0
//		config.useGL30 = true;
		
		// create the game
		new LwjglApplication(listener, config);
	}
}
