package com.eldritch.invoken.actor.ai;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Augmentation.Target;
import com.eldritch.invoken.actor.type.BasicLocatable;
import com.eldritch.invoken.actor.type.Npc;

public class TacticsManager {
    private final Map<Class<?>, Waypoint> waypoints = new HashMap<>();
    private final Target target = new Target();
    private final Npc npc;
    private Augmentation chosen = null;
    
    public TacticsManager(Npc npc) {
        this.npc = npc;
    }
    
    public void update(float delta) {
        for (Waypoint waypoint : waypoints.values()) {
            waypoint.setUpdated(false);
        }
    }
    
    public void setChosen(Augmentation chosen) {
        this.chosen = chosen;
    }
    
    public boolean hasChosen() {
        return chosen != null;
    }
    
    public Augmentation getChosen() {
        return chosen;
    }
    
    public Target getTarget() {
        return target;
    }
    
    public void setTarget(Target target) {
        this.target.set(target);
    }
    
    public void setWaypoint(Class<?> key, Vector2 point) {
        if (waypoints.containsKey(key)) {
            waypoints.get(key).reset(point);
        } else {
            waypoints.put(key, new Waypoint(point));
        }
    }
    
    public void removeWaypoint(Class<?> key) {
        waypoints.get(key).setActive(false);
    }
    
    public boolean hasWaypoint(Class<?> key) {
        return waypoints.containsKey(key) && waypoints.get(key).isActive();
    }
    
    public Waypoint getWaypoint(Class<?> key) {
        return waypoints.get(key);
    }
    
    /**
     * A waypoint is almost identical to a basic locatable with one exception: in order to avoid
     * having to recreate instances, we have an active flag that allows us to enable and disable
     * waypoints.
     */
    public static class Waypoint extends BasicLocatable {
        // should we allow navigation to this point?
        private boolean active = true;
        
        // was this point just updated this cycle?
        private boolean updated = false;
        
        public Waypoint(Vector2 point) {
            super(point);
        }
        
        public void reset(Vector2 point) {
            setPosition(point);
            active = true;
            updated = true;
        }
        
        public void setActive(boolean value) {
            this.active = value;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public boolean wasUpdated() {
            return updated;
        }
        
        private void setUpdated(boolean value) {
            this.updated = value;
        }
    }
}
