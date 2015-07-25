package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Strings;

public class Automaton extends Npc {
    public static float MAX_VELOCITY = 4f;

    private Automaton(NonPlayerActor data, float x, float y, float width, float height,
            float velocity, Map<Activity, Map<Direction, Animation>> animations, Level level) {
        super(data, x, y, width, height, velocity, animations, level);
    }

    @Override
    public float getDamageScale(DamageType damage) {
        switch (damage) {
            case PHYSICAL:
                // very resistant
                return 0.5f;
            case THERMAL:
            case TOXIC:
                // weak
                return 1.5f;
            case RADIOACTIVE:
            case VIRAL:
                // immune
                return 0;
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
        return 2;
    }

    @Override
    public boolean isVisible(Agent other) {
        // automatons do not see visible light, but other spectra
        return hasLineOfSight(other);
    }

    private static String getAssetPath(String asset) {
        return "sprite/characters/automaton/" + asset;
    }

    private static String getAsset(NonPlayerActor data) {
        return !Strings.isNullOrEmpty(data.getParams().getBodyType()) ? data.getParams()
                .getBodyType() : "mech1";
    }

    public static Automaton from(NonPlayerActor data, float x, float y, Level level) {
        String asset = getAsset(data);

        String base = asset;
        int index = asset.indexOf("/");
        if (index >= 0) {
            base = asset.substring(0, index);
        }

        switch (base) {
            case "android":
                return new Android(data, x, y, getAssetPath(asset), level);
            case "drone":
                return new Drone(data, x, y, getAssetPath(asset), level);
            case "goliath":
                return new Goliath(data, x, y, getAssetPath(asset), level);
            default:
                return new Mech(data, x, y, getAssetPath(asset), level);
        }
    }

    public static class Android extends Automaton {
        private static final int PX = 64;

        public Android(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    AnimationUtils.getHumanAnimations(asset + ".png"), level);
        }
    }

    public static class Drone extends Automaton {
        private static final int PX = 200;

        public Drone(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, 1, 1, MAX_VELOCITY, AnimationUtils.forSingleSequence(asset, PX),
                    level);
        }

        @Override
        protected void draw(Batch batch, TextureRegion frame, Direction direction) {
            final float width = getWidth();
            final float height = getHeight();
            batch.draw(frame, // texture
                    position.x - width / 2, position.y - height / 2, // position
                    width / 2, height / 2, // origin
                    width, height, // size
                    1f, 1f, // scale
                    getWeaponSentry().getDirection().angle()); // rotation
        }
    }

    public static class Mech extends Automaton {
        private static final int PX = 64;
        private static float MAX_VELOCITY = 2f;

        public Mech(NonPlayerActor data, float x, float y, String asset, Level level) {
            this(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, PX, asset, level);
        }

        public Mech(NonPlayerActor data, float x, float y, float width, float height, int px, String asset,
                Level level) {
            super(data, x, y, width, height, MAX_VELOCITY, getAnimations(asset, px), level);
        }

        @Override
        public float getDensity() {
            return 25f;
        }

        @Override
        public float getBodyRadius() {
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

        private static Map<Activity, Map<Direction, Animation>> getAnimations(String assetName, int px) {
            Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

            TextureRegion[][] regions = GameScreen.getRegions(assetName + "-walk.png", px, px);
            animations.put(Activity.Cast, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Thrust, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Idle, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Explore, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Swipe, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Combat, AnimationUtils.getAnimations(regions));

            regions = GameScreen.getRegions(assetName + "-death.png", px, px);
            animations.put(Activity.Death, AnimationUtils.getFixedAnimation(regions));

            return animations;
        }
    }

    public static class Goliath extends Mech {
        private static final int PX = 96;
        
        public Goliath(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, PX, asset, level);
        }
    }
}
