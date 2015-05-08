package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Strings;

public class Beast extends Npc {
    public static float MAX_VELOCITY = 4f;
    public static int PX = 32;

    public Beast(NonPlayerActor data, float x, float y, float width, float height, float velocity,
            Map<Activity, Map<Direction, Animation>> animations, Location location) {
        super(data, x, y, width, height, velocity, animations, location);
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.GHOST_DEATH;
    }

    @Override
    public float getDensity() {
        return 3;
    }

    @Override
    public boolean isVisible(Agent other) {
        // automatons do not see visible light, but other spectra
        return hasLineOfSight(other);
    }

    private static String getAssetPath(String asset) {
        return "sprite/characters/beast/" + asset;
    }

    private static String getAsset(NonPlayerActor data) {
        return !Strings.isNullOrEmpty(data.getParams().getBodyType()) ? data.getParams()
                .getBodyType() : "slime";
    }

    public static Beast from(NonPlayerActor data, float x, float y, Location location) {
        String asset = getAsset(data);
        switch (asset) {
            case "dragon":
                return new Dragon(data, x, y, getAssetPath(asset), location);
            case "parasite":
                return new Parasite(data, x, y, getAssetPath(asset), location);
            default:
                return new DefaultBeast(data, x, y, getAssetPath(asset), location);
        }
    }

    public static class DefaultBeast extends Beast {
        public DefaultBeast(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    getAllAnimations(asset), location);
        }

        private static Map<Activity, Map<Direction, Animation>> getAllAnimations(String assetName) {
            Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

            TextureRegion[][] regions = GameScreen.getRegions(assetName + "-walk.png", PX, PX);
            animations.put(Activity.Cast, getAnimations(regions));
            animations.put(Activity.Thrust, getAnimations(regions));
            animations.put(Activity.Explore, getAnimations(regions));
            animations.put(Activity.Swipe, getAnimations(regions));
            animations.put(Activity.Combat, getAnimations(regions));

            regions = GameScreen.getRegions(assetName + "-death.png", PX, PX);
            animations
                    .put(Activity.Death, getAnimations(regions, Animation.PlayMode.NORMAL, false));

            return animations;
        }

        private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions) {
            return getAnimations(regions, Animation.PlayMode.LOOP_PINGPONG, true);
        }

        private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
                Animation.PlayMode playMode, boolean increment) {
            Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

            // up, left, down, right
            int index = 0;
            for (Direction d : Direction.values()) {
                Animation anim = new Animation(0.15f, regions[index]);
                anim.setPlayMode(playMode);
                animations.put(d, anim);
                if (increment) {
                    index++;
                }
            }

            return animations;
        }
    }

    public static class Dragon extends Beast {
        private static final int WIDTH = 600;
        private static final int HEIGHT = 360;

        private Direction lastDirection;
        private boolean flipX;

        public Dragon(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, scale(WIDTH), scale(HEIGHT), MAX_VELOCITY, getAnimations(asset),
                    location);
        }

        @Override
        public float getDensity() {
            return 25f;
        }

        @Override
        protected float getBodyRadius() {
            return 0.4f;
        }

        @Override
        protected float getCombatWander() {
            return 0.5f;
        }

        @Override
        public float getDefaultAcceleration() {
            return 25 * getDensity();
        }

        @Override
        protected void draw(Batch batch, TextureRegion frame, Direction direction) {
            if (direction != lastDirection) {
                lastDirection = direction;

                // only change our direction if we are explicitly facing a different x direction
                // otherwise, keep the current horizontal heading
                if (direction == Direction.Right) {
                    flipX = true;
                } else if (direction == Direction.Left) {
                    flipX = false;
                }
            }

            float width = getWidth();
            float height = getHeight();
            batch.draw(frame.getTexture(), position.x - width / 2, position.y - height / 2, // position
                    width / 2, height / 2, // origin
                    width, height, // size
                    1f, 1f, // scale
                    0, // rotation
                    frame.getRegionX(), frame.getRegionY(), // srcX, srcY
                    frame.getRegionWidth(), frame.getRegionHeight(), // srcWidth, srcHeight
                    flipX, false); // flipX, flipY
        }

        private static float scale(float pixels) {
            return (pixels * Settings.SCALE) / 5f;
        }

        private static Map<Activity, Map<Direction, Animation>> getAnimations(String assetPath) {
            Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

            TextureRegion[][] regions = GameScreen.getRegions(assetPath + ".png", WIDTH, HEIGHT);

            animations.put(Activity.Cast, getAnimations(regions, 2));
            animations.put(Activity.Thrust, getAnimations(regions, 3));
            animations.put(Activity.Explore, getAnimations(regions, 1));
            animations.put(Activity.Swipe, getAnimations(regions, 3));
            animations.put(Activity.Combat, getAnimations(regions, 3));
            animations.put(Activity.Death, getAnimations(regions, 0, Animation.PlayMode.NORMAL));
            return animations;
        }

        private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int offset) {
            return getAnimations(regions, offset, Animation.PlayMode.LOOP_PINGPONG);
        }

        private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
                int offset, Animation.PlayMode playMode) {
            Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

            // up, left, down, right
            for (Direction d : Direction.values()) {
                Animation anim = new Animation(0.15f, regions[offset]);
                anim.setPlayMode(playMode);
                animations.put(d, anim);
            }

            return animations;
        }
    }

    public static class Parasite extends Beast {
        private static final int PX = 20;

        public Parasite(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    getAnimations(asset), location);
        }
        
        @Override
        public float getDensity() {
            return 5f;
        }

        private static Map<Activity, Map<Direction, Animation>> getAnimations(String assetPath) {
            Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

            TextureRegion[][] moveRegions = GameScreen.getRegions(assetPath + "-move.png", 20, 20);
            TextureRegion[][] attackRegions = GameScreen.getRegions(assetPath + "-attack.png", 20,
                    20);

            animations.put(Activity.Cast, getAnimations(attackRegions));
            animations.put(Activity.Thrust, getAnimations(attackRegions));
            animations.put(Activity.Explore, getAnimations(moveRegions));
            animations.put(Activity.Swipe, getAnimations(attackRegions));
            animations.put(Activity.Combat, getAnimations(moveRegions));
            animations.put(Activity.Death, getAnimations(moveRegions, Animation.PlayMode.NORMAL));
            return animations;
        }

        private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions) {
            return getAnimations(regions, Animation.PlayMode.LOOP_PINGPONG);
        }

        private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
                Animation.PlayMode playMode) {
            Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

            // up, left, down, right
            final int offset = 0;
            for (Direction d : Direction.values()) {
                Animation anim = new Animation(0.15f, regions[offset]);
                anim.setPlayMode(playMode);
                animations.put(d, anim);
            }

            return animations;
        }
    }
}
