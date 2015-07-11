package com.eldritch.invoken.util;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.ui.Tooltip;
import com.eldritch.invoken.ui.TooltipManager;

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
    
    public static Tooltip createTooltip(String text, Skin skin) {
        TooltipManager manager = new TooltipManager();
        manager.setMaxWidth(500);
        Tooltip tooltip = new Tooltip(text, manager, skin);
        tooltip.setInstant(true);
        tooltip.setAlways(true);
        return tooltip;
    }
    
    private Utils() {}
}
