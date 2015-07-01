package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Undead extends HumanNpc {
    public static float MAX_VELOCITY = 15f;
    public static float MAX_ACCELERATION = 10f;
    
    public Undead(NonPlayerActor data, float x, float y, String defaultAsset, Level level) {
        super(data, x, y, MAX_VELOCITY, getAsset(data, defaultAsset), level);
        setMaxLinearAcceleration(MAX_ACCELERATION);
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
    
    private static String getAsset(NonPlayerActor data, String defaultAsset) {
        if (data.getParams().hasBodyType()) {
            return String.format("sprite/characters/%s.png", data.getParams().getBodyType());
        }
        return defaultAsset;
    }
}
