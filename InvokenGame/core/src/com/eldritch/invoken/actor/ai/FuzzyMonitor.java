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
        limit = getLimit();
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
            limit = getLimit();
        }
    }

    public boolean isExpended() {
        return ready ? value > limit : value >= 0;
    }

    // randomize for more organic effect
    protected abstract float getLimit();

    public interface PenaltyFunction {
        float getPenalty();
    }
}
