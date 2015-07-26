package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.screens.GameScreen;

public class SummonEnergy extends AnimatedEffect {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(getAssets());

    public SummonEnergy(Agent target) {
        this(target, target.getForwardVector().scl(-1), 1, 1, 0.05f);
    }

    public SummonEnergy(Agent target, Vector2 offset, float width, float height, float frameDuration) {
        super(target, regions, offset, width, height, frameDuration);
    }

    @Override
    protected void doApply() {
    }

    @Override
    public void dispel() {
    }

    public static AnimatedEntity getEntity(Agent target, float size, float frameDuration) {
        return new AnimatedEntity(regions, target.getPosition(), new Vector2(size, size),
                frameDuration, Float.POSITIVE_INFINITY);
    }

    private static String[] getAssets() {
        String[] assets = new String[19];
        for (int i = 0; i < 19; i++) {
            assets[i] = format(i + 1);
        }
        return assets;
    }

    private static String format(int i) {
        return "sprite/effects/charge/charge" + i + ".png";
    }
}
