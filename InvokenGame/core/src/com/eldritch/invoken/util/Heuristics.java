package com.eldritch.invoken.util;

public class Heuristics {
    private Heuristics() {}
    
    public static float randomizedDistanceScore(float value, float target) {
        float score = Heuristics.distanceScore(value, target);
        return (float) (Math.random() * score);
    }
    
    public static float distanceScore(float value, float target) {
        float x = Math.abs(value - target);
        return (float) (1.0f / Math.pow(x, 0.4));
    }
}
