package com.eldritch.invoken.util;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Utils {
    public static Vector2 getSize(TextureRegion region, float majorSize) {
        return new Vector2(getWidth(region, majorSize), getHeight(region, majorSize));
    }
    
    public static float getWidth(TextureRegion region, float majorSize) {
        if (region.getRegionWidth() >= region.getRegionHeight()) {
            return majorSize;
        }
        return (majorSize * region.getRegionWidth()) / region.getRegionHeight();
    }
    
    public static float getHeight(TextureRegion region, float majorSize) {
        if (region.getRegionHeight() >= region.getRegionWidth()) {
            return majorSize;
        }
        return (majorSize * region.getRegionHeight()) / region.getRegionWidth();
    }
    
    private Utils() {}
}
