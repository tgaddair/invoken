package com.eldritch.invoken.util;

import static com.eldritch.invoken.util.SoundManager.CodingFormat.*;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.eldritch.invoken.InvokenGame;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A service that manages the sound effects.
 */
public class SoundManager implements Disposable {
    private static final String BASE_PATH = "sound";
    private static final float MAX_DST2 = 100f;

    /**
     * The available sound files.
     */
    public enum SoundEffect {
        CLICK(WAV, "click"),
        FOOTSTEP(OGG, "footstep00", "footstep01"),
        HIT(OGG, "hit00"),
        DOOR_OPEN(WAV, "door-open"),
        RANGED_WEAPON_SMALL(WAV, "ranged-weapon-small"),
        RANGED_WEAPON_LARGE(WAV, "ranged-weapon-large"),
        RANGED_WEAPON_SHOTGUN(WAV, "ranged-weapon-shotgun"),
        RANGED_WEAPON_RIFLE(WAV, "ranged-weapon-rifle"),
        SWISH(OGG, "swish");

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
            if (filenames.size() == 1) {
                return filenames.get(0);
            } else {
                return filenames.get((int) (Math.random() * filenames.size()));
            }
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
    
    private OrthographicCamera camera;

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
    
    public void playAtPoint(SoundEffect sound, Vector2 point) {
        playAtPoint(sound, 0, point);
    }
    
    public int playAtPoint(SoundEffect sound, int index, Vector2 point) {
        return playAtPoint(sound, index, point, 1);
    }
    
    public int playAtPoint(SoundEffect sound, int index, Vector2 point, float s) {
        float dst2 = point.dst2(camera.position.x, camera.position.y);
        if (dst2 > MAX_DST2) {
            // inaudible
            return index;
        }
        
        String filename = sound.getFilename(index);
        float dv = s * (MAX_DST2 - dst2) / MAX_DST2;
//        InvokenGame.logfmt("play at point: %.2f", dv);
        play(filename, dv);
        return sound.nextInSequence(index);
    }
    
    private void play(String filename) {
        play(filename, 1);
    }
    
    private void play(String filename, float dv) {
        // check if sound is enabled
        if (!enabled) {
            return;
        }

        try {
            Sound soundToPlay = sounds.get(filename);
            soundToPlay.play(volume * dv);
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
    
    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
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
