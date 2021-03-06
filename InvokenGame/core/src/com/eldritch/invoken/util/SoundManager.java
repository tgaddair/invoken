package com.eldritch.invoken.util;

import static com.eldritch.invoken.util.SoundManager.CodingFormat.EMPTY;
import static com.eldritch.invoken.util.SoundManager.CodingFormat.OGG;
import static com.eldritch.invoken.util.SoundManager.CodingFormat.WAV;

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
        NONE(EMPTY, ""),
        CLICK(WAV, "click"),
        COLLECT(OGG, "collect1"),
        FOOTSTEP(OGG, "footstep00", "footstep01"),
        HIT(OGG, "hit00"),
        MELEE_HIT(OGG, "melee-hit-00", "melee-hit-01"),
        MELEE_SWING(WAV, "melee-swing-00", "melee-swing-01", "melee-swing-02"),
        DOOR_OPEN(WAV, "door-open"),
        RANGED_WEAPON_SMALL(WAV, "ranged-weapon-small"),
        RANGED_WEAPON_LARGE(WAV, "ranged-weapon-large"),
        RANGED_WEAPON_SHOTGUN(WAV, "ranged-weapon-shotgun"),
        RANGED_WEAPON_RIFLE(WAV, "ranged-weapon-rifle"),
        RANGED_WEAPON_DRY(OGG, "ranged-weapon-dry"),
        RANGED_WEAPON_RELOAD_START(OGG, "ranged-weapon-reload-start"),
        RANGED_WEAPON_RELOAD_END(OGG, "ranged-weapon-reload-end"),
        CONSUMABLE(OGG, "consume"),
        SWISH(OGG, "swish"),
        BUFF(WAV, "buff"),
        GHOST_DEATH(OGG, "ghost-death"),
        HUMAN_DEATH(WAV, "human-death-00", "human-death-01", "human-death-02"),
        
        // Crawler
        CRAWLER_ATTACK(WAV, "crawler/attack"),
        CRAWLER_DEATH(WAV, "crawler/death"),
        
        INVENTORY_OFF(OGG, "inventory-off"),
        INVENTORY_ON(OGG, "inventory-on-00", "inventory-on-01"),
        INVALID(WAV, "invalid");

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
        
        public int randomIndex() {
            if (filenames.size() == 1) {
                return 0;
            }
            return (int) (Math.random() * filenames.size());
        }

        public String getFilename() {
            // the null result can never happen, by virtue of our constructor that ensures at least
            // one asset is specified
            return filenames.get(randomIndex());
        }

        private String format(String asset, CodingFormat coding) {
            return String.format("%s/%s.%s", BASE_PATH, asset, coding.getSuffix());
        }
    }

    enum CodingFormat {
        WAV, OGG, EMPTY;

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

    public int playInSequence(SoundEffect sound, int index) {
        play(sound, index, 1);
        return sound.nextInSequence(index);
    }
    
    public void playAtPoint(SoundEffect sound, Vector2 point) {
        playAtPoint(sound, sound.randomIndex(), point);
    }
    
    public int playAtPoint(SoundEffect sound, Vector2 point, float s) {
        return playAtPoint(sound, sound.randomIndex(), point, s);
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
        
        float dv = s * (MAX_DST2 - dst2) / MAX_DST2;
//        InvokenGame.logfmt("play at point: %.2f", dv);
        play(sound, index, dv);
        return sound.nextInSequence(index);
    }
    
    public void play(SoundEffect sound) {
        play(sound, 1f);
    }
    
    public void play(SoundEffect sound, float s) {
        play(sound, sound.randomIndex(), s);
    }
    
    private void play(SoundEffect sound, int index, float dv) {
        // check if sound is enabled
        if (!enabled) {
            return;
        }
        
        if (sound == SoundEffect.NONE){
            return;
        }
        
        String filename = sound.getFilename(index);
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
