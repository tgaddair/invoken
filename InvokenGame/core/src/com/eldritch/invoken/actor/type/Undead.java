package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Undead extends HumanNpc {
    private Undead(NonPlayerActor data, float x, float y, String asset, Level level,
            float maxVelocity, float maxAcceleration) {
        super(data, x, y, maxVelocity, asset, level);
        setMaxLinearAcceleration(maxAcceleration);
    }

    @Override
    public float getDamageScale(DamageType damage) {
        switch (damage) {
            default:
                return 1;
        }
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.GHOST_DEATH;
    }

    public static Undead from(NonPlayerActor data, float x, float y, Level level) {
        String asset = getAsset(data, "hollow-zombie");

        String base = asset;
        int index = asset.indexOf("/");
        if (index >= 0) {
            base = asset.substring(0, index);
        }

        switch (base) {
            case "reborn":
                return new Reborn(data, x, y, getAssetPath(asset), level);
            default:
                return new DefaultUndead(data, x, y, getAssetPath(asset), level);
        }
    }

    private static String getAsset(NonPlayerActor data, String defaultAsset) {
        if (data.getParams().hasBodyType()) {
            return data.getParams().getBodyType();
        }
        return defaultAsset;
    }

    private static String getAssetPath(String asset) {
        return String.format("sprite/characters/%s.png", asset);
    }

    private static class Reborn extends Undead {
        public static float MAX_VELOCITY = 10f;
        public static float MAX_ACCELERATION = 5f;

        private Reborn(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, asset, level, MAX_VELOCITY, MAX_ACCELERATION);
        }
    }

    private static class DefaultUndead extends Undead {
        public static float MAX_VELOCITY = 15f;
        public static float MAX_ACCELERATION = 10f;

        private DefaultUndead(NonPlayerActor data, float x, float y, String asset, Level level) {
            super(data, x, y, asset, level, MAX_VELOCITY, MAX_ACCELERATION);
        }
    }
}
