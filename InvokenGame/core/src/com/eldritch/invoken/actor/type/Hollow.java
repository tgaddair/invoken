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

public class Hollow extends Npc {
    public static float MAX_VELOCITY = 3f;

    private Hollow(NonPlayerActor data, float x, float y, float width, float height,
            float velocity, Map<Activity, Map<Direction, Animation>> animations, Level level) {
        super(data, x, y, width, height, velocity, animations, level);
    }

    @Override
    public float getDamageScale(DamageType damage) {
        switch (damage) {
            case VIRAL:
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

    private static String getAssetPath(String asset) {
        return "sprite/characters/hollow/" + asset;
    }

    private static String getAsset(NonPlayerActor data) {
        return !Strings.isNullOrEmpty(data.getParams().getBodyType()) ? data.getParams()
                .getBodyType() : "hollow";
    }

    public static Hollow from(NonPlayerActor data, float x, float y, Level level) {
        String asset = getAsset(data);

        String base = asset;
        int index = asset.indexOf("/");
        if (index >= 0) {
            base = asset.substring(0, index);
        }

        switch (base) {
            case "golem":
                return new Golem(data, x, y, getAssetPath(asset), level);
            default:
                return new Humanoid(data, x, y, getAssetPath(asset), level);
        }
    }

    public static class Golem extends Hollow {
        private static final int PX = 96;
        private static final int ATTACK_PX = 144;
        private static final float SCALE = 0.85f;

        public Golem(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, 2.5f, 2.5f, MAX_VELOCITY, getAnimations(asset), level);
        }
        
        @Override
        protected void draw(Batch batch, TextureRegion frame, Direction direction) {
            float width = frame.getRegionWidth() * Settings.SCALE * SCALE;
            float height = frame.getRegionHeight() * Settings.SCALE * SCALE;
            batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
        }
        
        @Override
        public float getAttackSpeed() {
            return 1f;
        }
        
        @Override
        public float getHoldSeconds() {
            return 1f;
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
            animations.put(Activity.Idle, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Explore, AnimationUtils.getAnimations(regions));
            
            regions = GameScreen.getRegions(assetName + "-attack.png", PX, ATTACK_PX);
            animations.put(Activity.Cast, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Thrust, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Swipe, AnimationUtils.getAnimations(regions));
            animations.put(Activity.Combat, AnimationUtils.getAnimations(regions));

            regions = GameScreen.getRegions(assetName + "-death.png", PX, PX);
            animations.put(Activity.Death, AnimationUtils.getFixedAnimation(regions));

            return animations;
        }
    }

    public static class Humanoid extends Hollow {
        private static final int PX = 64;

        public Humanoid(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, Settings.SCALE * PX, Settings.SCALE * PX, MAX_VELOCITY,
                    AnimationUtils.getHumanAnimations(asset + ".png"), level);
        }
    }
}
