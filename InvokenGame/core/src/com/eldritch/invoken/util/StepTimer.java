package com.eldritch.invoken.util;

public class StepTimer {
    private float value;
    private float limit;
    
    public void reset(float limit) {
        this.limit = limit;
        this.value = 0;
    }
    
    public void update(float delta) {
        if (!isFinished()) {
            value += delta;
        }
    }
    
    public boolean isFinished() {
        return value >= limit;
    }
}
