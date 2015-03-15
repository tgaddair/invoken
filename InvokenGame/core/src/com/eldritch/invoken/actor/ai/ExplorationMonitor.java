package com.eldritch.invoken.actor.ai;

public class ExplorationMonitor extends FuzzyMonitor {
    // base amount of exploration (in seconds) past which the NPC should return
    private static final float EXPLORED = 10;
    
    public ExplorationMonitor() {
        setToLimit();
    }

    @Override
    protected float getLimit() {
        return ((float) Math.random() * EXPLORED) + 1;
    }
}
