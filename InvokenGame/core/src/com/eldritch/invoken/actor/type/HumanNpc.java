package com.eldritch.invoken.actor.type;

import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class HumanNpc extends Npc {
    public HumanNpc(NonPlayerActor data, float x, float y, String asset, Location location) {
        this(data, x, y, Human.MAX_VELOCITY, asset, location);
    }

    public HumanNpc(NonPlayerActor data, float x, float y, Location location) {
        this(data, x, y, Human.MAX_VELOCITY, getBody(data), location);
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.HUMAN_DEATH;
    }

    public HumanNpc(NonPlayerActor data, float x, float y, float maxVelocity, String asset,
            Location location) {
        super(data, x, y, Human.getWidth(), Human.getHeight(), maxVelocity, AnimationUtils
                .getHumanAnimations(asset), location);
    }

    private static String getBody(NonPlayerActor data) {
        return String.format("sprite/characters/%s.png", data.getParams().getBodyType());
    }
}
