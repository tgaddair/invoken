package com.eldritch.invoken.actor;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

public interface Drawable {
    void render(float delta, OrthogonalTiledMapRenderer renderer);
    
    Vector2 getPosition();
}
