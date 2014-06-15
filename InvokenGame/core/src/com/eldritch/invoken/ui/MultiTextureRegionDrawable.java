package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MultiTextureRegionDrawable extends TextureRegionDrawable {
    private final TextureRegion[] layers;

    public MultiTextureRegionDrawable(TextureRegion base, TextureRegion... layers) {
        super(base);
        this.layers = layers;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        super.draw(batch, x, y, width, height);
        for (TextureRegion region : layers) {
            batch.draw(region, x, y, width, height);
        }
    }

    @Override
    public void draw(Batch batch, float x, float y, float originX, float originY, float width,
            float height, float scaleX, float scaleY, float rotation) {
        super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        for (TextureRegion region : layers) {
            batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        }
    }
}
