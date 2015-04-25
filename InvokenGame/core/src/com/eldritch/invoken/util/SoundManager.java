package com.eldritch.invoken.util;

import static com.eldritch.invoken.util.SoundManager.CodingFormat.*;

import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.eldritch.invoken.InvokenGame;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * A service that manages the sound effects.
 */
public class SoundManager implements Disposable {
    private static final String BASE_PATH = "sound";
    
    /**
     * The available sound files.
     */
    public enum SoundEffect {
        CLICK("click", WAV),
        FOOTSTEP("footstep00", OGG);

        private final String fileName;

        private SoundEffect(String asset, CodingFormat coding) {
            this.fileName = String.format("%s/%s.%s", BASE_PATH, asset, coding.getSuffix());
        }
        
        public String getFileName() {
            return fileName;
        }
    }
    
    enum CodingFormat {
        WAV, OGG;
        
        public String getSuffix() {
            return name().toLowerCase();
        }
    }

    /**
     * The volume to be set on the sound.
     */
    private float volume = 1f;

    /**
     * Whether the sound is enabled.
     */
    private boolean enabled = true;

    /**
     * The sound cache.
     */
    private final LoadingCache<String, Sound> sounds = CacheBuilder.newBuilder()
            .removalListener(new RemovalListener<String, Sound>() {
                @Override
                public void onRemoval(RemovalNotification<String, Sound> removal) {
                    Sound sound = removal.getValue();
                    sound.dispose();
                }
            }).build(new CacheLoader<String, Sound>() {
                public Sound load(String fileName) {
                    FileHandle soundFile = Gdx.files.internal(fileName);
                    return Gdx.audio.newSound(soundFile);
                }
            });

    /**
     * Plays the specified sound.
     */
    public void play(SoundEffect sound) {
        // check if sound is enabled
        if (!enabled) {
            return;
        }

        try {
            Sound soundToPlay = sounds.get(sound.fileName);
            soundToPlay.play(volume);
        } catch (ExecutionException e) {
            InvokenGame.error("Failed to load sound: " + sound.fileName, e);
        }
    }

    /**
     * Sets the sound volume which must be inside the range [0,1].
     */
    public void setVolume(float volume) {
        InvokenGame.log("Adjusting sound volume to: " + volume);

        // check and set the new volume
        if (volume < 0 || volume > 1f) {
            throw new IllegalArgumentException("The volume must be inside the range: [0,1]");
        }
        this.volume = volume;
    }

    /**
     * Enables or disabled the sound.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Disposes the sound manager.
     */
    @Override
    public void dispose() {
        InvokenGame.log("Disposing sound manager");
        for (Sound sound : sounds.asMap().values()) {
            sound.stop();
        }
        sounds.invalidateAll();
    }
}
