package com.eldritch.invoken.util;

public class Heuristics {
    private Heuristics() {}
    
    public static float distanceScore(float value, float target) {
        float x = Math.abs(value - target);
        return (float) (1.0f / Math.pow(x, 0.4));
    }
}
