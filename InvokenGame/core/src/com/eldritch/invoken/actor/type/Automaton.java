package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.screens.GameScreen;

public class Automaton extends Npc {
    public static float MAX_VELOCITY = 4f;
    public static int PX = 64;

    public Automaton(NonPlayerActor data, float x, float y, String asset, Location location) {
        super(data, x, y, 1 / 32f * PX, 1 / 32f * PX, MAX_VELOCITY, getAllAnimations(asset),
                location);
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

    public static Map<Activity, Map<Direction, Animation>> getAllAnimations(String assetName) {
        Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

        TextureRegion[][] regions = GameScreen.getRegions(assetName + "-walk.png", PX, PX);
        animations.put(Activity.Cast, getAnimations(regions));
        animations.put(Activity.Thrust, getAnimations(regions));
        animations.put(Activity.Explore, getAnimations(regions));
        animations.put(Activity.Swipe, getAnimations(regions));
        animations.put(Activity.Combat, getAnimations(regions));

        regions = GameScreen.getRegions(assetName + "-death.png", PX, PX);
        animations.put(Activity.Death, getAnimations(regions, Animation.PlayMode.NORMAL, false));

        return animations;
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions) {
        return getAnimations(regions, Animation.PlayMode.LOOP_PINGPONG, true);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
            Animation.PlayMode playMode, boolean increment) {
        Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        // up, left, down, right
        int index = 0;
        for (Direction d : Direction.values()) {
            Animation anim = new Animation(0.15f, regions[index]);
            anim.setPlayMode(playMode);
            animations.put(d, anim);
            if (increment) {
                index++;
            }
        }

        return animations;
    }
}
