package com.eldritch.invoken.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.eldritch.invoken.InvokenGame;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A service that manages the background music.
 * <p>
 * Only one music may be playing at a given time.
 */
public class MusicManager implements Disposable {
    private final LoadingCache<MusicTrack, BackgroundMusic> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<MusicTrack, BackgroundMusic>() {
                public BackgroundMusic load(MusicTrack track) {
                    return new BackgroundMusic(track);
                }
            });

    public enum MusicTrack {
        MAIN("main.ogg"), //
        LEVEL0("level0.ogg"), //
        COMBAT0("combat0_loop.ogg", "combat0_end.ogg"), //
        COMBAT1("combat1.ogg", "combat1_end.ogg", 16f), //
        CREDITS("sweet_ice.ogg"); // alt: credits.ogg

        private final String asset;
        private final Optional<String> end;
        private final float inflectionPoint;

        private MusicTrack(String asset) {
            this(asset, Optional.<String> absent(), 0);
        }

        private MusicTrack(String asset, String end) {
            this(asset, Optional.of(end), 0);
        }

        private MusicTrack(String asset, String end, float inflectionPoint) {
            this(asset, Optional.of(end), inflectionPoint);
        }

        private MusicTrack(String asset, Optional<String> end, float inflectionPoint) {
            this.asset = asset;
            this.end = end;
            this.inflectionPoint = inflectionPoint;
        }

        public String getAsset() {
            return asset;
        }

        public String getEnd() {
            return end.get();
        }

        public boolean hasEnd() {
            return end.isPresent();
        }

        public float getInflectionPoint() {
            return inflectionPoint;
        }
    }

    // private constants
    private static final float FADE_DURATION = 5f;
    private static final float MIN_TRANSITION_TIME = 10f;

    /**
     * The available music files.
     */
    public class BackgroundMusic {
        private final MusicTrack track;
        private final Music musicResource;
        private final Optional<Music> conclusion;

        private BackgroundMusic(MusicTrack track) {
            this.track = track;
            musicResource = loadMusic(track.getAsset());

            if (track.hasEnd()) {
                conclusion = Optional.of(loadMusic(track.getEnd()));
            } else {
                conclusion = Optional.absent();
            }
        }

        public String getAsset() {
            return track.getAsset();
        }

        public MusicTrack getTrack() {
            return track;
        }

        public Music getMusicResource() {
            return musicResource;
        }

        public Music getConclusionResource() {
            return conclusion.get();
        }

        public boolean hasConclusion() {
            return conclusion.isPresent();
        }
    }

    private static Music loadMusic(String asset) {
        String fileName = "music/" + asset;
        FileHandle musicFile = Gdx.files.internal(fileName);
        return Gdx.audio.newMusic(musicFile);
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

    private final List<MusicHandler> handlers = new LinkedList<>();

    /**
     * Creates the music manager.
     */
    public MusicManager() {
    }

    public void update(float delta) {
        Iterator<MusicHandler> it = handlers.iterator();
        while (it.hasNext()) {
            MusicHandler handler = it.next();
            handler.update(delta);
            if (handler.isFinished()) {
                handler.dispose();
                it.remove();
            }
        }
    }

    public void fadeIn(MusicTrack track) {
        // check if the music is enabled
        if (!enabled) {
            return;
        }

        // check if the given music is already being played
        BackgroundMusic music = getMusic(track);
        if (musicBeingPlayed == music) {
            return;
        }

        if (musicBeingPlayed != null
                && musicBeingPlayed.getMusicResource().getPosition() < MIN_TRANSITION_TIME) {
            // avoid constantly thrashing between tracks
            return;
        }

        // do some logging
        InvokenGame.log("Fading in music: " + music.getAsset());

        // TODO: stop any music being faded, if not handled already

        // start streaming the new music
        Music musicResource = music.getMusicResource();
        musicResource.setVolume(0);
        musicResource.setLooping(true);
        musicResource.play();

        // construct a new fader
        handlers.add(new Fader(musicBeingPlayed, FADE_DURATION));

        // set the music being played
        musicBeingPlayed = music;
    }

    public void playPostConclusion(MusicTrack track) {
        // check if the music is enabled
        if (!enabled) {
            return;
        }

        // check if the given music is already being played
        BackgroundMusic music = getMusic(track);
        if (musicBeingPlayed == music) {
            return;
        }
        
        if (musicBeingPlayed != null
                && musicBeingPlayed.getMusicResource().getPosition() < MIN_TRANSITION_TIME) {
            // avoid constantly thrashing between tracks
            return;
        }

        if (musicBeingPlayed == null
                || musicBeingPlayed.getMusicResource().getPosition() < musicBeingPlayed.getTrack()
                        .getInflectionPoint()) {
            fadeIn(track);
        } else {
            // do some logging
            InvokenGame.log("Concluding and starting music: " + music.getAsset());

            // construct a new concluder
            handlers.add(new Concluder(musicBeingPlayed, music));

            // set the music being played
            musicBeingPlayed = music;
        }
    }

    /**
     * Plays the given music (starts the streaming).
     * <p>
     * If there is already a music being played it is stopped automatically.
     */
    public void play(MusicTrack track) {
        // check if the music is enabled
        if (!enabled) {
            return;
        }

        // check if the given music is already being played
        BackgroundMusic music = getMusic(track);
        if (musicBeingPlayed == music) {
            return;
        }
        
        if (musicBeingPlayed != null
                && musicBeingPlayed.getMusicResource().getPosition() < MIN_TRANSITION_TIME) {
            // avoid constantly thrashing between tracks
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

        if (music.hasConclusion()) {
            musicResource = music.getConclusionResource();
            musicResource.stop();
            // musicResource.dispose();
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

    private BackgroundMusic getMusic(MusicTrack track) {
        if (musicBeingPlayed != null && track == musicBeingPlayed.getTrack()) {
            return musicBeingPlayed;
        }

        try {
            return cache.get(track);
        } catch (ExecutionException e) {
            InvokenGame.error("Failed to load music!", e);
            return musicBeingPlayed;
        }
    }

    private class Fader implements MusicHandler {
        private final BackgroundMusic outgoingMusic;
        private final float duration;
        private float elapsed = 0;

        public Fader(BackgroundMusic outgoingMusic, float duration) {
            this.outgoingMusic = outgoingMusic;
            this.duration = duration;
        }

        @Override
        public void update(float delta) {
            elapsed += delta;

            float progress = Math.max(Math.min(elapsed / duration, 1f), 0f);
            float fraction = MathUtils.lerp(0, volume, progress);
            musicBeingPlayed.getMusicResource().setVolume(fraction);
            outgoingMusic.getMusicResource().setVolume(volume - fraction);
        }

        @Override
        public boolean isFinished() {
            return elapsed > duration;
        }

        @Override
        public void dispose() {
            stop(outgoingMusic);
        }
    }

    private class Concluder implements MusicHandler, OnCompletionListener {
        private final BackgroundMusic outgoingMusic;
        private final BackgroundMusic music;
        private boolean finished = false;

        public Concluder(BackgroundMusic outgoingMusic, BackgroundMusic music) {
            this.outgoingMusic = outgoingMusic;
            this.music = music;

            if (outgoingMusic.hasConclusion()) {
                outgoingMusic.getMusicResource().stop();

                Music musicResource = outgoingMusic.getConclusionResource();
                musicResource.setVolume(volume);
                musicResource.setLooping(false);
                musicResource.setOnCompletionListener(this);
                musicResource.play();
            } else {
                finished = true;
            }
        }

        @Override
        public void update(float delta) {
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void dispose() {
            stop(outgoingMusic);

            // start streaming the new music
            if (music == musicBeingPlayed) {
                Music musicResource = music.getMusicResource();
                musicResource.setVolume(volume);
                musicResource.setLooping(true);
                musicResource.play();
            }
        }

        @Override
        public void onCompletion(Music music) {
            finished = true;
        }
    }

    private interface MusicHandler {
        void update(float delta);

        boolean isFinished();

        void dispose();
    }
}
