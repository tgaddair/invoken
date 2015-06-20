package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class HumanNpc extends Npc {
    public HumanNpc(NonPlayerActor data, float x, float y, String asset, Level level) {
        this(data, x, y, Human.MAX_VELOCITY, asset, level);
    }

    public HumanNpc(NonPlayerActor data, float x, float y, Level level) {
        this(data, x, y, Human.MAX_VELOCITY, getBody(data), level);
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.HUMAN_DEATH;
    }

    public HumanNpc(NonPlayerActor data, float x, float y, float maxVelocity, String asset,
            Level level) {
        super(data, x, y, Human.getWidth(), Human.getHeight(), maxVelocity, AnimationUtils
                .getHumanAnimations(asset), level);
    }

    private static String getBody(NonPlayerActor data) {
        return String.format("sprite/characters/%s.png", data.getParams().getBodyType());
    }
}
