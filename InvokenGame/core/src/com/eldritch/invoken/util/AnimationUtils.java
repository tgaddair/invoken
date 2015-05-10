package com.eldritch.invoken.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.screens.GameScreen;

public class AnimationUtils {
    public final static int HUMAN_PX = 64;

    private AnimationUtils() {
    }
    
    public static Map<Activity, Map<Direction, Animation>> forSingleSequence(String assetName, int px) {
        Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();

        TextureRegion[][] regions = GameScreen.getRegions(assetName + ".png", px, px);
        animations.put(Activity.Cast, AnimationUtils.getAnimations(regions, false));
        animations.put(Activity.Thrust, AnimationUtils.getAnimations(regions, false));
        animations.put(Activity.Idle, AnimationUtils.getAnimations(regions, false));
        animations.put(Activity.Explore, AnimationUtils.getAnimations(regions, false));
        animations.put(Activity.Swipe, AnimationUtils.getAnimations(regions, false));
        animations.put(Activity.Combat, AnimationUtils.getAnimations(regions, false));
        animations.put(Activity.Death, AnimationUtils.getAnimations(regions, false));

        return animations;
    }

    public static Map<Activity, Map<Direction, Animation>> getHumanAnimations(String assetName) {
        Map<Activity, Map<Direction, Animation>> animations = new HashMap<Activity, Map<Direction, Animation>>();
        TextureRegion[][] regions = GameScreen.getRegions(assetName, HUMAN_PX, HUMAN_PX);

        // cast
        int offset = 0;
        animations.put(Activity.Cast, AnimationBuilder.from(regions).setOffset(offset).setEndX(7)
                .build());

        // thrust
        offset += Direction.values().length;
        animations.put(Activity.Thrust, AnimationBuilder.from(regions).setOffset(offset).setEndX(8)
                .build());

        // walk
        offset += Direction.values().length;
        animations.put(Activity.Idle, AnimationBuilder.from(regions).setOffset(offset).setX(0)
                .setEndX(1).setPlayMode(Animation.PlayMode.LOOP).build());
        
        animations.put(Activity.Explore, AnimationBuilder.from(regions).setOffset(offset).setX(1)
                .setEndX(9).setPlayMode(Animation.PlayMode.LOOP).build());

        // swipe
        offset += Direction.values().length;
        animations.put(Activity.Swipe, AnimationBuilder.from(regions).setOffset(offset).setEndX(6)
                .build());

        // shoot
        offset += Direction.values().length;
        animations.put(Activity.Combat, AnimationBuilder.from(regions).setOffset(offset)
                .setEndX(13).build());

        // hurt
        offset += Direction.values().length;
        animations.put(Activity.Death, AnimationBuilder.from(regions).setOffset(offset).setEndX(6)
                .setExplicitDirections(false).setPlayMode(Animation.PlayMode.NORMAL).build());

        return animations;
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

    public static Map<Direction, Animation> getAnimations(TextureRegion[][] regions, int x,
            int length, int offset, boolean increment, Animation.PlayMode playMode) {
        int index = offset;
        Map<Direction, Animation> directions = new HashMap<Direction, Animation>();
        for (Direction d : Direction.values()) {
            TextureRegion[] textures = Arrays.copyOfRange(regions[index], x, length);
            Animation anim = new Animation(Settings.FRAME_DURATION, textures);
            anim.setPlayMode(playMode);
            directions.put(d, anim);
            if (increment) {
                index++;
            }
        }
        return directions;
    }

    public static class AnimationBuilder {
        private final TextureRegion[][] regions;
        private int offset = 0;
        private int x = 0;
        private int endX;
        private Animation.PlayMode playMode = Animation.PlayMode.LOOP_PINGPONG;
        private boolean explicitDirections = true;

        private AnimationBuilder(TextureRegion[][] regions) {
            this.regions = regions;
            endX = regions[0].length;
        }

        public int getOffset() {
            return offset;
        }

        public AnimationBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public int getX() {
            return x;
        }

        public AnimationBuilder setX(int x) {
            this.x = x;
            return this;
        }

        public int getEndX() {
            return endX;
        }

        public AnimationBuilder setEndX(int endX) {
            this.endX = endX;
            return this;
        }

        public Animation.PlayMode getPlayMode() {
            return playMode;
        }

        public AnimationBuilder setPlayMode(Animation.PlayMode playMode) {
            this.playMode = playMode;
            return this;
        }

        public boolean getExplicitDirections() {
            return explicitDirections;
        }

        public AnimationBuilder setExplicitDirections(boolean explicitDirections) {
            this.explicitDirections = explicitDirections;
            return this;
        }

        public TextureRegion[][] getRegions() {
            return regions;
        }

        public Map<Direction, Animation> build() {
            int index = offset;
            Map<Direction, Animation> directions = new HashMap<Direction, Animation>();
            for (Direction d : Direction.values()) {
                TextureRegion[] textures = regions[index];
                if (x > 0 || endX < textures.length) {
                    textures = Arrays.copyOfRange(textures, x, endX);
                }

                Animation anim = new Animation(Settings.FRAME_DURATION, textures);
                anim.setPlayMode(playMode);
                directions.put(d, anim);
                if (explicitDirections) {
                    index++;
                }
            }
            return directions;
        }

        public static AnimationBuilder from(TextureRegion[][] regions) {
            return new AnimationBuilder(regions);
        }
    }
}
