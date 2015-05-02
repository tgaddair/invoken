package com.eldritch.invoken.actor.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;

public class Human {
    public static float MAX_VELOCITY = 8f;
    
    // figure out the width and height of the player for collision
    // detection and rendering by converting a player frames pixel
    // size into world units (1 unit == 32 pixels)
    public static int PX = 64;
    
    
    public static float getWidth() {
        return 1 / 32f * PX;
    }
    
    public static float getHeight() {
        return 1 / 32f * PX;
    }
    
    public static Animation getAnimation(String assetName) {
        TextureRegion[][] regions = GameScreen.getRegions(assetName, PX, PX);
        Animation anim = new Animation(0.15f, regions[0]);
        return anim;
    }
    
    public static Map<Activity, Map<Direction, Animation>> getDefaultAnimations() {
        return getAllAnimations("sprite/characters/light-blue-hair.png");
    }
    
    public static Map<Direction, Animation> getAnimations(String assetName) {
    	return getAnimations(assetName, PX);
    }

    public static Map<Direction, Animation> getAnimations(String assetName, int px) {
        Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        // up, left, down, right
        TextureRegion[][] regions = GameScreen.getRegions(assetName, px, px);
        for (Direction d : Direction.values()) {
            Animation anim = new Animation(0.15f, regions[d.ordinal()]);
            anim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            animations.put(d, anim);
        }

        return animations;
    }

    public static Map<Activity, Map<Direction, Animation>> getAllAnimations(String assetName) {
        Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();
        TextureRegion[][] regions = GameScreen.getRegions(assetName, PX, PX);

        // cast
        int offset = 0;
        animations.put(Activity.Cast, getAnimations(regions, 7, offset));

        // thrust
        offset += Direction.values().length;
        animations.put(Activity.Thrust, getAnimations(regions, 8, offset));

        // walk
        offset += Direction.values().length;
        animations.put(Activity.Explore, getAnimations(regions, 9, offset));

        // swipe
        offset += Direction.values().length;
        animations.put(Activity.Swipe, getAnimations(regions, 6, offset));

        // shoot
        offset += Direction.values().length;
        animations.put(Activity.Combat, getAnimations(regions, 13, offset));

        // hurt
        offset += Direction.values().length;
        animations.put(Activity.Death,
                getAnimations(regions, 6, offset, false, Animation.PlayMode.NORMAL));

        return animations;
    }

    private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int length,
            int offset) {
        return getAnimations(regions, length, offset, true, Animation.PlayMode.LOOP);
    }

    private static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int length,
            int offset, boolean increment, Animation.PlayMode playMode) {
        int index = offset;
        Map<Direction, Animation> directions = new HashMap<Direction, Animation>();
        for (Direction d : Direction.values()) {
            TextureRegion[] textures = Arrays.copyOfRange(regions[index], 0, length);
            Animation anim = new Animation(Settings.FRAME_DURATION, textures);
            anim.setPlayMode(playMode);
            directions.put(d, anim);
            if (increment) {
                index++;
            }
        }
        return directions;
    }
    
    private Human() {}
}
