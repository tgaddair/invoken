package com.eldritch.invoken.actor;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.encounter.Location;

public interface Entity {
    void update(float delta, Location location);
    
    void render(float delta, OrthogonalTiledMapRenderer renderer);
}
