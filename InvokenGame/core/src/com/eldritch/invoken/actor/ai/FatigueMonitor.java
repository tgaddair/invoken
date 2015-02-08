package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Npc;

/**
 * Fuzzy logic switch that has a fatigue meter that builds up.  Once it passes the threshold, it
 * has to come back down to 0 before it can go back up.
 */
public class FatigueMonitor {
    private static final float TIRED = 10;  // seconds of activity
    
    private final Npc npc;
    private float limit;
    private boolean rested = true;  // below the threshold
    private float fatigue = 0;
    
    public FatigueMonitor(Npc npc) {
        this.npc = npc;
        limit = (float) Math.random() * TIRED;  // randomize for more organize effect
    }
    
    public void addFatigue(float delta) {
        if (delta > 0) {
            // adding fatigue, so pay the endurance penalty
            delta += .001f * (1f - npc.getInfo().getEndurance());
        }
        fatigue += delta;
        
        // check to see if we're rested or not
        if (fatigue > limit) {
            rested = false;
        } else if (fatigue <= 0) {
            rested = true;
            limit = (float) Math.random() * TIRED;
        }
    }
    
    public boolean isTired() {
        return rested ? fatigue > limit : fatigue >= 0;
    }
}
