package com.eldritch.invoken.actor.type;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;

public class Human {
    public static float MAX_VELOCITY = 10f;
    
    // figure out the width and height of the player for collision
    // detection and rendering by converting a player frames pixel
    // size into world units (1 unit == 32 pixels)
    public static int PX = 64;
    
    
    public static float getWidth() {
        return Settings.SCALE * PX;
    }
    
    public static float getHeight() {
        return Settings.SCALE * PX;
    }
    
    public static Animation getAnimation(String assetName) {
        TextureRegion[][] regions = GameScreen.getRegions(assetName, PX, PX);
        Animation anim = new Animation(0.15f, regions[0]);
        return anim;
    }
    
    public static Map<Activity, Map<Direction, Animation>> getDefaultAnimations() {
        return AnimationUtils.getHumanAnimations("sprite/characters/light-blue-hair.png");
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
    
    private Human() {}
}
