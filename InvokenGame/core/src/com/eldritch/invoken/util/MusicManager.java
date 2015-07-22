package com.eldritch.invoken.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.eldritch.invoken.InvokenGame;
import com.google.common.base.Optional;

/**
 * A service that manages the background music.
 * <p>
 * Only one music may be playing at a given time.
 */
public class MusicManager implements Disposable {
    public static final String MAIN = "main.ogg";
    
    public static final String LEVEL0 = "level0.ogg";

    public static final String COMBAT0 = "combat0.ogg";
    
//    public static final String CREDITS = "credits.ogg";
    public static final String CREDITS = "sweet_ice.ogg";
    
    // private constants
    private static final float FADE_DURATION = 5f;
    
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
    
    private Optional<Fader> fader = Optional.absent();

    /**
     * Creates the music manager.
     */
    public MusicManager() {
    }
    
    public void update(float delta) {
        if (fader.isPresent()) {
            fader.get().update(delta);
            if (fader.get().isFinished()) {
                fader.get().dispose();
                fader = Optional.absent();
            }
        }
    }
    
    public void fadeIn(String asset) {
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
        InvokenGame.log("Fading in music: " + music.getAsset());

        // stop any music being faded
        if (fader.isPresent()) {
            fader.get().dispose();
        }

        // start streaming the new music
        Music musicResource = music.getMusicResource();
        musicResource.setVolume(0);
        musicResource.setLooping(true);
        musicResource.play();
        
        // construct a new fader
        fader = Optional.of(new Fader(musicBeingPlayed, FADE_DURATION));

        // set the music being played
        musicBeingPlayed = music;
    }
    
    public void playPostConclusion(String asset) {
        
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
            stop(musicBeingPlayed);
            musicBeingPlayed = null;
        }
    }
    
    private void stop(BackgroundMusic music) {
        Music musicResource = music.getMusicResource();
        musicResource.stop();
        musicResource.dispose();
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
    
    public class Fader {
        private final BackgroundMusic outgoingMusic;
        private final float duration;
        private float elapsed = 0;
        
        public Fader(BackgroundMusic outgoingMusic, float duration) {
            this.outgoingMusic = outgoingMusic;
            this.duration = duration;
        }
        
        public void update(float delta) {
            elapsed += delta;
            
            float progress = Math.max(Math.min(elapsed / duration, 1f), 0f);
            float fraction = MathUtils.lerp(0, volume, progress);
            musicBeingPlayed.getMusicResource().setVolume(fraction);
            outgoingMusic.getMusicResource().setVolume(volume - fraction);
        }
        
        public boolean isFinished() {
            return elapsed > duration;
        }
        
        public void dispose() {
            stop(outgoingMusic);
        }
    }
}
