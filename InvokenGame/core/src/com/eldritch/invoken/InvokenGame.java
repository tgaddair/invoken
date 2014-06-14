package com.eldritch.invoken;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.screens.MenuScreen;
import com.eldritch.invoken.screens.SplashScreen;
import com.eldritch.invoken.util.ActorMarshaller;
import com.eldritch.invoken.util.AugmentationMarshaller;
import com.eldritch.invoken.util.FactionMarshaller;
import com.eldritch.invoken.util.ItemMarshaller;
import com.eldritch.invoken.util.LocationMarshaller;

public class InvokenGame extends Game {
	public static final String LOG = InvokenGame.class.getSimpleName();
	public static boolean DEV_MODE = true;
	
	public final static ActorMarshaller ACTOR_READER = new ActorMarshaller();
	public final static AugmentationMarshaller AUG_READER = new AugmentationMarshaller();
	public final static ItemMarshaller ITEM_READER = new ItemMarshaller();
	public final static FactionMarshaller FACTION_READER = new FactionMarshaller();
	public final static LocationMarshaller LOCATION_READER = new LocationMarshaller();

	SpriteBatch batch;
	Texture img;

	public SplashScreen getSplashScreen() {
		return new SplashScreen(this);
	}

	public MenuScreen getMenuScreen() {
		return new MenuScreen(this);
	}

	@Override
	public void create() {
		Gdx.app.log(InvokenGame.LOG, "Creating game on " + Gdx.app.getType());

		// // create the preferences manager preferencesManager = new
		// PreferencesManager();
		//
		// // create the music manager musicManager = new MusicManager();
		// musicManager.setVolume(preferencesManager.getVolume());
		// musicManager.setEnabled(preferencesManager.isMusicEnabled());
		//
		// // create the sound manager soundManager = new SoundManager();
		// soundManager.setVolume(preferencesManager.getVolume());
		// soundManager.setEnabled(preferencesManager.isSoundEnabled());
		//
		// // create the profile manager profileManager = new ProfileManager();
		// profileManager.retrieveProfile();
		//
		// // create the level manager levelManager = new LevelManager();
		//
		// // create the helper objects fpsLogger = new FPSLogger();
	}

	@Override
	public void render() {
		super.render();
		
		// output the current FPS
		if (DEV_MODE) {
			// fpsLogger.log();
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		Gdx.app.log(InvokenGame.LOG, "Resizing game to: " + width + " x "
				+ height);

		// show the splash screen when the game is resized for the first time;
		// this approach avoids calling the screen's resize method repeatedly
		if (getScreen() == null) {
			if (DEV_MODE) {
				setScreen(new GameScreen(this));
			} else {
				setScreen(new SplashScreen(this));
			}
		}
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
		Gdx.app.log(InvokenGame.LOG, "Setting screen: "
				+ screen.getClass().getSimpleName());
	}
	
	public static void log(String text) {
		Gdx.app.log(InvokenGame.LOG, text);
	}
	
	public static void error(String text, Exception ex) {
	    Gdx.app.error(InvokenGame.LOG, text, ex);
	}
}
