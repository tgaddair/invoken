package com.eldritch.invoken.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent.Direction;

public class AnimationUtils {
    private AnimationUtils() {
    }

    public static Map<Direction, Animation> getFixedAnimation(TextureRegion[][] regions) {
        return getAnimations(regions, Animation.PlayMode.NORMAL, 0, false);
    }

    public static Map<Direction, Animation> getAnimationsNoDirections(TextureRegion[][] regions) {
        return getAnimations(regions, false);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions) {
        return getAnimations(regions, true);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
            boolean directions) {
        return getAnimations(regions, Animation.PlayMode.LOOP_PINGPONG, 0, directions);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int offset) {
        return getAnimations(regions, Animation.PlayMode.LOOP_PINGPONG, offset);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
            Animation.PlayMode playMode, int offset) {
        return getAnimations(regions, playMode, offset, false);
    }

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions,
            Animation.PlayMode playMode, int offset, boolean directions) {
        Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        // up, left, down, right
        int index = 0;
        for (Direction d : Direction.values()) {
            Animation anim = new Animation(0.15f, regions[offset + index]);
            anim.setPlayMode(playMode);
            animations.put(d, anim);
            if (directions) {
                index++;
            }
        }

        return animations;
    }
}
