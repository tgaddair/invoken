package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

public class NormalMappedTile extends StaticTiledMapTile {
    private final TextureRegion normalRegion;
    
    public NormalMappedTile(TextureRegion textureRegion, TextureRegion normalRegion) {
        super(textureRegion);
        this.normalRegion = normalRegion;
    }
    
    public Texture getNormal() {
        return normalRegion.getTexture();
    }
    
    public TextureRegion getNormalRegion() {
        return normalRegion;
    }
}
