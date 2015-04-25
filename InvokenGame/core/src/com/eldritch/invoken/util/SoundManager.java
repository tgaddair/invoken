package com.eldritch.invoken.util;

import static com.eldritch.invoken.util.SoundManager.CodingFormat.*;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.eldritch.invoken.InvokenGame;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * A service that manages the sound effects.
 */
public class SoundManager implements Disposable {
    private static final String BASE_PATH = "sound";

    /**
     * The available sound files.
     */
    public enum SoundEffect {
        CLICK(WAV, "click"), FOOTSTEP(OGG, "footstep00", "footstep01");

        private final ImmutableList<String> filenames;

        private SoundEffect(final CodingFormat coding, String asset, String... assets) {
            filenames = ImmutableList.<String> builder().add(format(asset, coding))
                    .addAll(Lists.transform(Arrays.asList(assets), new Function<String, String>() {
                        @Override
                        public String apply(String asset) {
                            return format(asset, coding);
                        }
                    })).build();
        }
        
        public int nextInSequence(int index) {
            return (index + 1) % filenames.size();
        }
        
        public String getFilename(int index) {
            return filenames.get(index);
        }

        public String getFilename() {
            // the null result can never happen, by virtue of our constructor that ensures at least
            // one asset is specified
            return Iterables.getFirst(filenames, null);
        }

        private String format(String asset, CodingFormat coding) {
            return String.format("%s/%s.%s", BASE_PATH, asset, coding.getSuffix());
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
        play(sound.getFilename());
    }
    
    public int playInSequence(SoundEffect sound, int index) {
        String filename = sound.getFilename(index);
        play(filename);
        return sound.nextInSequence(index);
    }
    
    private void play(String filename) {
        // check if sound is enabled
        if (!enabled) {
            return;
        }

        try {
            Sound soundToPlay = sounds.get(filename);
            soundToPlay.play(volume);
        } catch (ExecutionException e) {
            InvokenGame.error("Failed to load sound: " + filename, e);
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
