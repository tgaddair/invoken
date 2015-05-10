package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.AnimationUtils.AnimationBuilder;
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
    public float getDamageScale(DamageType damage) {
        switch (damage) {
            case PHYSICAL:
            case RADIOACTIVE:
            case VIRAL:
                // weak
                return 1.25f;
            case THERMAL:
            case TOXIC:
                // resistant
                return 0.5f;
            default:
                return 1;
        }
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

        String base = asset;
        int index = asset.indexOf("/");
        if (index >= 0) {
            base = asset.substring(0, index);
        }

        switch (base) {
            case "crawler":
                return new Crawler(data, x, y, getAssetPath(asset), location);
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
            animations.put(Activity.Cast, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Thrust, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Explore, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Swipe, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Combat, AnimationUtils.getAnimations(regions));

            regions = GameScreen.getRegions(assetName + "-death.png", PX, PX);
            animations.put(Activity.Death, AnimationUtils.getFixedAnimation(regions));

            return animations;
        }
    }

    public static class Crawler extends Beast {
        private static final int PX = 64;

        public Crawler(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    getAnimations(asset), location);
        }
        
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                case TOXIC:
                    // immune
                    return 0f;
                default:
                    return 1;
            }
        }
        
        @Override
        protected short getCategoryBits() {
            return Settings.BIT_LOW_AGENT;
        }

        private static Map<Activity, Map<Direction, Animation>> getAnimations(String assetPath) {
            Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

            TextureRegion[][] regions = GameScreen.getRegions(assetPath + ".png", PX, PX);
            Map<Direction, Animation> move = AnimationBuilder.from(regions)
                    .setPlayMode(Animation.PlayMode.LOOP).setX(4).build();
            Map<Direction, Animation> attack = AnimationBuilder.from(regions).setEndX(4)
                    .build();
            Map<Direction, Animation> death = AnimationBuilder.from(regions)
                    .setPlayMode(Animation.PlayMode.NORMAL).setOffset(4).setEndX(4)
                    .setExplicitDirections(false).build();

            animations.put(Activity.Cast, attack);
            animations.put(Activity.Thrust, attack);
            animations.put(Activity.Explore, move);
            animations.put(Activity.Swipe, attack);
            animations.put(Activity.Combat, attack);
            animations.put(Activity.Death, death);
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

            animations.put(Activity.Cast, AnimationUtils.getAnimations(regions, 2));
            animations.put(Activity.Thrust, AnimationUtils.getAnimations(regions, 3));
            animations.put(Activity.Explore, AnimationUtils.getAnimations(regions, 1));
            animations.put(Activity.Swipe, AnimationUtils.getAnimations(regions, 3));
            animations.put(Activity.Combat, AnimationUtils.getAnimations(regions, 3));
            animations.put(Activity.Death,
                    AnimationUtils.getAnimations(regions, Animation.PlayMode.NORMAL, 0));
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

            TextureRegion[][] moveRegions = GameScreen.getRegions(assetPath + "-move.png", PX, PX);
            TextureRegion[][] attackRegions = GameScreen.getRegions(assetPath + "-attack.png", PX,
                    PX);

            animations.put(Activity.Cast, AnimationUtils.getAnimations(attackRegions));
            animations.put(Activity.Thrust, AnimationUtils.getAnimations(attackRegions));
            animations.put(Activity.Explore, AnimationUtils.getAnimations(moveRegions));
            animations.put(Activity.Swipe, AnimationUtils.getAnimations(attackRegions));
            animations.put(Activity.Combat, AnimationUtils.getAnimations(moveRegions));
            animations.put(Activity.Death, AnimationUtils.getFixedAnimation(moveRegions));
            return animations;
        }
    }
}
