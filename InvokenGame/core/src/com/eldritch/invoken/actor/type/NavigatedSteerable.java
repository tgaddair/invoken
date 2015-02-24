package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.layer.LocationMap;

public class NavigatedSteerable extends BasicSteerable {
    private final LocationMap map;
    
    public NavigatedSteerable(LocationMap map) {
        this.map = map;
    }
    
    public void setPosition(Vector2 position) {
    	super.setPosition(position);
    }
    
    @Override
    public Vector2 getPosition() {
        return super.getPosition();
    }
    
    public Vector2 getTarget() {
        return super.getPosition();
    }
}
