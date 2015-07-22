package com.eldritch.invoken.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
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
    public enum MusicTrack {
        MAIN("main.ogg"), //
        LEVEL0("level0.ogg"), //
        COMBAT0("combat0.ogg", 11.465f, 356.7f), //
        CREDITS("sweet_ice.ogg"); // alt: credits.ogg

        private final String asset;
        private final float start;
        private final Optional<Float> end;

        private MusicTrack(String asset) {
            this(asset, 0, Optional.<Float> absent());
        }

        private MusicTrack(String asset, float start, float end) {
            this(asset, start, Optional.of(end));
        }

        private MusicTrack(String asset, float start, Optional<Float> end) {
            this.asset = asset;
            this.start = start;
            this.end = end;
        }

        public String getAsset() {
            return asset;
        }

        public float getStart() {
            return start;
        }

        public float getEnd() {
            return end.get();
        }

        public boolean hasEnd() {
            return end.isPresent();
        }
    }

    // private constants
    private static final float FADE_DURATION = 5f;

    /**
     * The available music files.
     */
    public class BackgroundMusic {
        private final MusicTrack track;
        private final String fileName;
        private final Music musicResource;

        private BackgroundMusic(MusicTrack track) {
            this.track = track;
            this.fileName = "music/" + track.getAsset();

            FileHandle musicFile = Gdx.files.internal(fileName);
            musicResource = Gdx.audio.newMusic(musicFile);
        }

        public String getAsset() {
            return track.getAsset();
        }

        public String getFileName() {
            return fileName;
        }

        public MusicTrack getTrack() {
            return track;
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

        // do some logging
        InvokenGame.log("Concluding and starting music: " + music.getAsset());

        // construct a new concluder
        handlers.add(new Concluder(musicBeingPlayed, music));

        // set the music being played
        musicBeingPlayed = music;
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

    private BackgroundMusic getMusic(MusicTrack track) {
        if (musicBeingPlayed != null && track == musicBeingPlayed.getTrack()) {
            return musicBeingPlayed;
        }
        return new BackgroundMusic(track);
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

            MusicTrack track = outgoingMusic.getTrack();
            if (track.hasEnd()) {
                Music musicResource = outgoingMusic.getMusicResource();
                musicResource.setPosition(track.getEnd());
                musicResource.setLooping(false);
                musicResource.setOnCompletionListener(this);
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
            Music musicResource = music.getMusicResource();
            musicResource.setVolume(volume);
            musicResource.setLooping(true);
            musicResource.play();
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
