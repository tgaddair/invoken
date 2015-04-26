package com.eldritch.invoken;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.screens.MenuScreen;
import com.eldritch.invoken.screens.SplashScreen;
import com.eldritch.invoken.util.ActorMarshaller;
import com.eldritch.invoken.util.ContainerMarshaller;
import com.eldritch.invoken.util.FactionMarshaller;
import com.eldritch.invoken.util.ItemMarshaller;
import com.eldritch.invoken.util.LocationMarshaller;
import com.eldritch.invoken.util.MusicManager;
import com.eldritch.invoken.util.RoomMarshaller;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager;
import com.eldritch.invoken.util.TerminalMarshaller;

public class InvokenGame extends Game {
	public static final String LOG = InvokenGame.class.getSimpleName();
	
	public final static ActorMarshaller ACTOR_READER = new ActorMarshaller();
	public final static ItemMarshaller ITEM_READER = new ItemMarshaller();
	public final static FactionMarshaller FACTION_READER = new FactionMarshaller();
	public final static LocationMarshaller LOCATION_READER = new LocationMarshaller();
	public final static RoomMarshaller ROOM_READER = new RoomMarshaller();
	public final static ContainerMarshaller CONTAINER_READER = new ContainerMarshaller();
	public final static TerminalMarshaller TERMINAL_READER = new TerminalMarshaller();
	
	public static MusicManager MUSIC_MANAGER;
	public static SoundManager SOUND_MANAGER;

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
		
		MUSIC_MANAGER = new MusicManager();
		SOUND_MANAGER = new SoundManager();

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
		if (Settings.SKIP_MENU) {
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
			if (Settings.SKIP_MENU) {
				setScreen(new GameScreen(this, Profession.getDefault()));
			} else {
				setScreen(new MenuScreen(this));
			}
		}
	}
	
	@Override
	public void dispose() {
	    MUSIC_MANAGER.dispose();
	    SOUND_MANAGER.dispose();
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
	
	public static void logfmt(String text, Object... args) {
        Gdx.app.log(InvokenGame.LOG, String.format(text, args));
    }
	
	public static void error(String text, Exception ex) {
	    Gdx.app.error(InvokenGame.LOG, text, ex);
	}
}
