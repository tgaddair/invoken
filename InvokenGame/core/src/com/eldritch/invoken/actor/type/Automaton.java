package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;

public class Automaton extends Npc {
    public static float MAX_VELOCITY = 4f;
    public static int PX = 64;

    public Automaton(NonPlayerActor data, float x, float y, String asset, Location location) {
        super(data, x, y, 1 / 32f * PX, 1 / 32f * PX, getAllAnimations(asset), location);
    }
    

    @Override
    public float getMaxVelocity() {
        return MAX_VELOCITY;
    }

    public static Map<Activity, Map<Direction, Animation>> getAllAnimations(String assetName) {
        Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

        TextureRegion[][] regions = GameScreen.getRegions(assetName + "-walk.png", PX, PX);
        animations.put(Activity.Cast, getAnimations(regions));
        animations.put(Activity.Thrust, getAnimations(regions));
        animations.put(Activity.Explore, getAnimations(regions));
        animations.put(Activity.Swipe, getAnimations(regions));
        animations.put(Activity.Combat, getAnimations(regions));

        regions = GameScreen.getRegions(assetName + "-death.png", PX, PX);
        animations.put(Activity.Death, getAnimations(regions, false));

        return animations;
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions) {
        return getAnimations(regions, true);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
            boolean increment) {
        Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        // up, left, down, right
        int index = 0;
        for (Direction d : Direction.values()) {
            Animation anim = new Animation(0.15f, regions[index]);
            anim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            animations.put(d, anim);
            if (increment) {
                index++;
            }
        }

        return animations;
    }
}
