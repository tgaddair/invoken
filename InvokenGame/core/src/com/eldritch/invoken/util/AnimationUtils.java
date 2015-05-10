package com.eldritch.invoken.util;

import java.util.Arrays;
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
