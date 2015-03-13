package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.util.StepTimer;

public class ThreatMonitor {
    private static final float SUSPICION_SECS = 60;
    private static final float ALERT_SECS = 60;
    
    private static final float SUSPICION_RADIUS = 10;
    private static final float ALERT_RADIUS = 3;
    
    private final Npc npc;
    private final StepTimer suspicion = new StepTimer();
    private final StepTimer alert = new StepTimer();
    
    public ThreatMonitor(Npc npc) {
        this.npc = npc;
    }
    
    public void update(float delta) {
        suspicion.update(delta);
        alert.update(delta);
    }
    
    public void setSuspicious() {
        suspicion.reset(SUSPICION_SECS);
    }
    
    public boolean isSuspicious() {
        return !suspicion.isFinished();
    }
    
    public boolean isSuspiciousOf(Agent other) {
        float r = SUSPICION_RADIUS;
        return npc.dst2(other) < r * r;
    }
    
    public void setAlerted() {
        alert.reset(ALERT_SECS);
    }
    
    public boolean isAlerted() {
        return !alert.isFinished();
    }
    
    public boolean isAlertedTo(Agent other) {
        float r = ALERT_RADIUS;
        return npc.dst2(other) < r * r;
    }
}
