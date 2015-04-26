package com.eldritch.invoken.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.eldritch.invoken.InvokenGame;

/**
 * A service that manages the background music.
 * <p>
 * Only one music may be playing at a given time.
 */
public class MusicManager implements Disposable {
    public static final String MAIN = "main.ogg";
    
    /**
     * The available music files.
     */
    public class BackgroundMusic {
        private final String asset;
        private final String fileName;
        private final Music musicResource;

        private BackgroundMusic(String asset) {
            this.asset = asset;
            this.fileName = "music/" + asset;
            
            FileHandle musicFile = Gdx.files.internal(fileName);
            musicResource = Gdx.audio.newMusic(musicFile);
        }
        
        public String getAsset() {
            return asset;
        }

        public String getFileName() {
            return fileName;
        }

        public Music getMusicResource() {
            return musicResource;
        }
    }

    /**
     * Holds the music currently being played, if any.
     */
    private BackgroundMusic musicBeingPlayed;

    /**
     * The volume to be set on the music.
     */
    private float volume = 1f;

    /**
     * Whether the music is enabled.
     */
    private boolean enabled = true;

    /**
     * Creates the music manager.
     */
    public MusicManager() {
    }

    /**
     * Plays the given music (starts the streaming).
     * <p>
     * If there is already a music being played it is stopped automatically.
     */
    public void play(String asset) {
        // check if the music is enabled
        if (!enabled) {
            return;
        }

        // check if the given music is already being played
        BackgroundMusic music = getMusic(asset);
        if (musicBeingPlayed == music) {
            return;
        }

        // do some logging
        InvokenGame.log("Playing music: " + music.getAsset());

        // stop any music being played
        stop();

        // start streaming the new music
        Music musicResource = music.getMusicResource();
        musicResource.setVolume(volume);
        musicResource.setLooping(true);
        musicResource.play();

        // set the music being played
        musicBeingPlayed = music;
    }

    /**
     * Stops and disposes the current music being played, if any.
     */
    public void stop() {
        if (musicBeingPlayed != null) {
            InvokenGame.log("Stopping current music");
            Music musicResource = musicBeingPlayed.getMusicResource();
            musicResource.stop();
            musicResource.dispose();
            musicBeingPlayed = null;
        }
    }

    /**
     * Sets the music volume which must be inside the range [0,1].
     */
    public void setVolume(float volume) {
        InvokenGame.log("Adjusting music volume to: " + volume);

        // check and set the new volume
        if (volume < 0 || volume > 1f) {
            throw new IllegalArgumentException("The volume must be inside the range: [0,1]");
        }
        this.volume = volume;

        // if there is a music being played, change its volume
        if (musicBeingPlayed != null) {
            musicBeingPlayed.getMusicResource().setVolume(volume);
        }
    }

    /**
     * Enables or disabled the music.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        // if the music is being deactivated, stop any music being played
        if (!enabled) {
            stop();
        }
    }

    /**
     * Disposes the music manager.
     */
    public void dispose() {
        InvokenGame.log("Disposing music manager");
        stop();
    }
    
    private BackgroundMusic getMusic(String asset) {
        if (musicBeingPlayed != null && asset.equals(musicBeingPlayed.asset)) {
            return musicBeingPlayed;
        }
        return new BackgroundMusic(asset);
    }
}
