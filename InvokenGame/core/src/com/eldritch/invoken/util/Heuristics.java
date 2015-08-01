package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.type.Agent;

public class Heuristics {
    private Heuristics() {
    }
    
    public static float threatScore(Agent agent, Agent target, float idealDst2) {
        float distanceScore = distanceScore(agent.dst2(target), idealDst2);
        float threatScore = target.getInfo().getLevel();
        return distanceScore * threatScore;
    }

    public static float randomizedDistanceScore(float value, float target) {
        float score = Heuristics.distanceScore(value, target);
        return (float) (Math.random() * score);
    }

    public static float distanceScore(float value, float target) {
        float x = Math.abs(value - target);
        return (float) (1.0f / Math.pow(x, 0.4));
    }

    public static float getDesperation(Agent agent) {
        return agent.getInfo().getEnergyPercent() * (1.0f - agent.getInfo().getHealthPercent());
    }
    
    public static float sigmoid(float x) {
        return (float) (1f / (1f + Math.exp(-x)));
    }
}
