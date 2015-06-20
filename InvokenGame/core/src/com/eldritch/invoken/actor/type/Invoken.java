package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Invoken extends HumanNpc {
    public static float MAX_VELOCITY = 15f;
    public static float MAX_ACCELERATION = 10f;
    
    public Invoken(NonPlayerActor data, float x, float y, String asset, Level level) {
        super(data, x, y, MAX_VELOCITY, asset, level);
        setMaxLinearAcceleration(MAX_ACCELERATION);
    }
    
    @Override
    public float getDamageScale(DamageType damage) {
        switch (damage) {
            case VIRAL:
                // immune
                return 0;
            default:
                // very resistant to everything else
                return 0.1f;
        }
    }
    
    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.GHOST_DEATH;
    }
}
