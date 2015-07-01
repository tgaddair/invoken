package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class SummonEnergy extends AnimatedEffect {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(getAssets());

    public SummonEnergy(Agent target) {
        super(target, regions, target.getForwardVector().scl(-1), 1, 1, 0.05f);
    }

    @Override
    protected void doApply() {
    }

    @Override
    public void dispel() {
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
