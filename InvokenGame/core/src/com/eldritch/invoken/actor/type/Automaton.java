package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.Beast.Crawler;
import com.eldritch.invoken.actor.type.Beast.DefaultBeast;
import com.eldritch.invoken.actor.type.Beast.Dragon;
import com.eldritch.invoken.actor.type.Beast.Parasite;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Strings;

public class Automaton extends Npc {
    public static float MAX_VELOCITY = 4f;

    public Automaton(NonPlayerActor data, float x, float y, float width, float height, float velocity,
            Map<Activity, Map<Direction, Animation>> animations, Location location) {
        super(data, x, y, width, height, velocity, animations, location);
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

    public static Automaton from(NonPlayerActor data, float x, float y, Location location) {
        String asset = getAsset(data);

        String base = asset;
        int index = asset.indexOf("/");
        if (index >= 0) {
            base = asset.substring(0, index);
        }

        switch (base) {
            case "android":
                return new Android(data, x, y, getAssetPath(asset), location);
            case "drone":
                return new Drone(data, x, y, getAssetPath(asset), location);
            default:
                return new Mech(data, x, y, getAssetPath(asset), location);
        }
    }
    
    public static class Android extends Automaton {
        private static final int PX = 64;

        public Android(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    AnimationUtils.getHumanAnimations(asset + ".png"), location);
        }
    }
    
    public static class Drone extends Automaton {
        private static final int PX = 200;

        public Drone(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, 1, 1, MAX_VELOCITY,
                    AnimationUtils.forSingleSequence(asset, PX), location);
        }
    }

    public static class Mech extends Automaton {
        private static final int PX = 64;

        public Mech(NonPlayerActor data, float x, float y, String asset, Location location) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    getAnimations(asset), location);
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

        private static Map<Activity, Map<Direction, Animation>> getAnimations(String assetName) {
            Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

            TextureRegion[][] regions = GameScreen.getRegions(assetName + "-walk.png", PX, PX);
            animations.put(Activity.Cast, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Thrust, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Idle, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Explore, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Swipe, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Combat, AnimationUtils.getAnimations(regions));

            regions = GameScreen.getRegions(assetName + "-death.png", PX, PX);
            animations.put(Activity.Death, AnimationUtils.getFixedAnimation(regions));

            return animations;
        }
    }
}
