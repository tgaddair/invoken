package com.eldritch.invoken.actor.ai;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.util.ThreatMonitor;
import com.eldritch.invoken.util.StepTimer;

public class NpcThreatMonitor extends ThreatMonitor<Npc> {
    private static final float DELTA = 0.01f;  // state machine update frequency, 10 times a second
    private static final float SUSPICION_SECS = 60;
    private static final float ALERT_SECS = 60;

    private static final float SUSPICION_RADIUS = 10;
    private static final float ALERT_RADIUS = 3;

    private final StepTimer suspicion = new StepTimer();
    private final StepTimer alert = new StepTimer();

    private final StateMachine<Npc> threatLevel;
    private float lastStep = 0;

    public NpcThreatMonitor(Npc npc) {
        super(npc);
        threatLevel = new DefaultStateMachine<Npc>(npc, ThreatLevel.Calm);
    }

    public void update(float delta) {
        lastStep += delta;
        if (lastStep > DELTA) {
            threatLevel.update();
            lastStep = 0;
        }
    }

    @Override
    public void notice(Agent enemy) {
        threatLevel.handleMessage(Notice.of(enemy));
    }

    public void setSuspicious() {
        threatLevel.changeState(ThreatLevel.Suspicious);
    }

    public boolean isSuspicious() {
        return threatLevel.getCurrentState() == ThreatLevel.Suspicious;
    }

    public boolean isSuspiciousOf(Agent other) {
        float r = SUSPICION_RADIUS;
        return getAgent().dst2(other) < r * r;
    }

    public void setAlerted() {
        threatLevel.changeState(ThreatLevel.Suspicious);
    }

    public boolean isAlerted() {
        return threatLevel.getCurrentState() == ThreatLevel.Alerted;
    }

    public boolean isAlertedTo(Agent other) {
        float r = ALERT_RADIUS;
        return getAgent().dst2(other) < r * r;
    }

    private enum ThreatLevel implements State<Npc> {
        Calm() {
            @Override
            public void enter(Npc entity) {
            }
            
            @Override
            public void update(Npc entity) {
            }
            
            @Override
            protected void notice(Npc npc, Agent noticed) {
                NpcThreatMonitor monitor = npc.getThreat();
                if (monitor.isSuspiciousOf(noticed)) {
                    // calm -> suspicious
                    monitor.setSuspicious();
                }
            }
        },
        
        Suspicious() {
            @Override
            public void enter(Npc entity) {
                entity.getThreat().suspicion.reset(SUSPICION_SECS);
            }
            
            @Override
            public void update(Npc entity) {
                NpcThreatMonitor monitor = entity.getThreat();
                monitor.suspicion.update(DELTA);
            }
            
            @Override
            protected void notice(Npc npc, Agent noticed) {
                NpcThreatMonitor monitor = npc.getThreat();
                if (monitor.isAlertedTo(noticed)) {
                    // suspicious -> alerted
                    monitor.setAlerted();
                    monitor.addEnemy(noticed);
                }
            }
        },
        
        Alerted() {
            @Override
            public void enter(Npc entity) {
                entity.getThreat().alert.reset(ALERT_SECS);
            }
            
            @Override
            public void update(Npc entity) {
                NpcThreatMonitor monitor = entity.getThreat();
                monitor.alert.update(DELTA);
            }
            
            @Override
            protected void notice(Npc npc, Agent noticed) {
                NpcThreatMonitor monitor = npc.getThreat();
                monitor.addEnemy(noticed);
            }
        };

        @Override
        public void exit(Npc entity) {
        }

        @Override
        public boolean onMessage(Npc entity, Telegram telegram) {
            if (telegram instanceof Notice) {
                Notice notice = (Notice) telegram;
                notice(entity, notice.noticed);
                return true;
            }
            return false;
        }
        
        protected abstract void notice(Npc npc, Agent noticed);
    }
    
    private static class Notice extends Telegram {
        private final Agent noticed;
        
        public Notice(Agent noticed) {
            this.noticed = noticed;
        }
        
        public static Notice of(Agent noticed) {
            return new Notice(noticed);
        }
    }
}
