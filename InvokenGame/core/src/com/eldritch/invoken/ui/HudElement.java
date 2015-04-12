package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.location.Location;

public interface HudElement {
    Table getContainer();
    
    void resize(int width, int height);
    
    void update(float delta, Location location);
}
