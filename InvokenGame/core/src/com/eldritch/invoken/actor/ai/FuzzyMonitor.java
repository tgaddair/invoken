package com.eldritch.invoken.actor.ai;

/**
 * Fuzzy logic switch that has a meter that builds up. Once it passes the threshold, it has to come
 * back down to 0 before it can go back up.
 */
public abstract class FuzzyMonitor {
    private final PenaltyFunction penalty;
    private float limit;
    private boolean ready = true; // below the threshold
    private float value = 0;

    public FuzzyMonitor(PenaltyFunction pentalty) {
        this.penalty = pentalty;
        limit = penalty.getLimit();
    }
    
    public void useUntilLimit(float delta) {
        if (delta > 0 && !isExpended()) {
            use(delta);
        } else if (delta < 0 && isExpended()) {
            use(delta);
        }
    }

    public void use(float delta) {
        if (delta > 0) {
            // adding fatigue, so pay the endurance penalty
            delta += penalty.getPenalty();
        }
        value += delta;

        // check to see if we're rested or not
        if (value >= limit) {
            ready = false;
        } else if (value <= 0) {
            ready = true;
            limit = penalty.getLimit();
        }
    }
    
    public float getValue() {
        return value;
    }
    
    public float getPercent() {
        return value / limit;
    }

    public boolean isExpended() {
        return ready ? value >= limit : value >= 0;
    }
    
    public void setToLimit() {
        value = limit;
        ready = false;
    }
    
    public void setReady() {
        value = 0;
        ready = true;
    }

    public interface PenaltyFunction {
        float getPenalty();
        
        float getLimit();
    }
    
    public static class DefaultPenaltyFunction implements PenaltyFunction {
        @Override
        public float getPenalty() {
            return 0;
        }

        @Override
        public float getLimit() {
            return 0;
        }
    };
}
