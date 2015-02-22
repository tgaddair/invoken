package com.eldritch.invoken.actor.ai;

/**
 * Fuzzy logic switch that has a meter that builds up.  Once it passes the threshold, it
 * has to come back down to 0 before it can go back up.
 */
public class FuzzyMonitor {
    private final float threshold;  // seconds of activity
    private final PenaltyFunction penalty;
    private float limit;
    private boolean ready = true;  // below the threshold
    private float value = 0;
    
    public FuzzyMonitor(float threshold, PenaltyFunction pentalty) {
        this.threshold = threshold;
        this.penalty = pentalty;
        limit = (float) Math.random() * threshold;  // randomize for more organic effect
    }
    
    public void use(float delta) {
        if (delta > 0) {
            // adding fatigue, so pay the endurance penalty
            delta += penalty.getPenalty();
        }
        value += delta;
        
        // check to see if we're rested or not
        if (value > limit) {
            ready = false;
        } else if (value <= 0) {
            ready = true;
            limit = (float) Math.random() * threshold;
        }
    }
    
    public boolean isExpended() {
        return ready ? value > limit : value >= 0;
    }
    
    public interface PenaltyFunction {
        float getPenalty();
    }
}
