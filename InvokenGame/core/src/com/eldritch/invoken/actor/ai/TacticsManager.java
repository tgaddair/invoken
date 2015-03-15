package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class TacticsManager {
    private static final float CHARGE_RANGE = 5;
    private static final float CHARGE_SCALE = 2;
    
    private final Npc npc;
    private final float baseAcceleration;
    private final float baseVelocity;
    
    private boolean charging = false;
    
    public TacticsManager(Npc npc) {
        this.npc = npc;
        this.baseAcceleration = npc.getMaxLinearAcceleration();
        this.baseVelocity = npc.getMaxLinearSpeed();
    }
    
    public void update(float delta) {
        if (charging && !canCharge()) {
            setCharging(false);
        }
    }
    
    public void setCharging(boolean charging) {
        this.charging = charging;
        
        // must be idempotent
        if (charging) {
            npc.setMaxLinearAcceleration(baseAcceleration * CHARGE_SCALE);
            npc.setMaxLinearSpeed(baseVelocity * CHARGE_SCALE);
        } else {
            npc.setMaxLinearAcceleration(baseAcceleration);
            npc.setMaxLinearSpeed(baseVelocity);
        }
    }
    
    public boolean isCharging() {
        return charging;
    }
    
    public boolean canCharge() {
        if (!npc.hasTarget()) {
            return false;
        }
        
        Agent target = npc.getTarget();
        float threshold = CHARGE_RANGE;
        return npc.dst2(target) < threshold * threshold;
    }
}
