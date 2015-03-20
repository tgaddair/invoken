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
    private static final float DELTA = 0.01f; // state machine update frequency, 10 times a second
    private static final float SUSPICION_SECS = 20;
    private static final float ALERT_SECS = 20;

    public static final float SUSPICION_RADIUS = 7;
    public static final float ALERT_RADIUS = 3;

    private final StepTimer suspicion = new StepTimer();
    private final StepTimer alert = new StepTimer();

    private final StateMachine<Npc> threatLevel;
    private float lastStep = 0;

    public NpcThreatMonitor(Npc npc) {
        super(npc);
        threatLevel = new DefaultStateMachine<Npc>(npc, ThreatLevel.Calm);
    }

    public void update(float delta) {
        super.update(delta);
        
        lastStep += delta;
        if (lastStep > DELTA) {
            threatLevel.update();
            lastStep = 0;
        }
    }

    public void notifyOf(Agent enemy) {
        threatLevel.handleMessage(Notice.of(enemy));
    }

    public void setCalm() {
        threatLevel.changeState(ThreatLevel.Calm);
    }
    
    public boolean isCalm() {
        return threatLevel.getCurrentState() == ThreatLevel.Calm;
    }
    
    public void maybeSetSuspicious() {
        if (!isAlerted()) {
            setSuspicious();
        }
    }

    public void setSuspicious() {
        threatLevel.changeState(ThreatLevel.Suspicious);
    }
    
    public void setSuspicious(Agent other) {
        setSuspicious();
        getAgent().alertTo(other);
    }

    public boolean isSuspicious() {
        return threatLevel.getCurrentState() == ThreatLevel.Suspicious;
    }
    
    public boolean isSuspiciousOf(Agent other) {
        float r = SUSPICION_RADIUS;
        return getAgent().dst2(other) < r * r;
    }

    public void setAlerted() {
        threatLevel.changeState(ThreatLevel.Alerted);
    }

    public void setAlerted(Agent other) {
        setAlerted();
        getAgent().alertAllies(other);
    }

    public boolean isAlerted() {
        return threatLevel.getCurrentState() == ThreatLevel.Alerted;
    }
    
    public boolean isCombatReady() {
        return isAlerted() && hasEnemies();
    }

    public boolean isAlertedTo(Agent other) {
        float r = ALERT_RADIUS;
        return getAgent().dst2(other) < r * r;
    }
    
    public float getAwareness() {
        return getLevel().getAwareness();
    }
    
    public ThreatLevel getLevel() {
        return (ThreatLevel) threatLevel.getCurrentState();
    }

    public enum ThreatLevel implements State<Npc> {
        /**
         * Calm NPCs do not attack and are much easier to slip past.  They idle often.
         */
        Calm() {
            @Override
            public void enter(Npc entity) {
            }

            @Override
            public void afterUpdate(Npc entity) {
            }

            @Override
            protected void notice(Npc npc, Agent noticed) {
                NpcThreatMonitor monitor = npc.getThreat();
                if (monitor.isAlertedTo(noticed)) {
                    // calm -> alerted
                    monitor.setAlerted(noticed);
                } else if (monitor.isSuspiciousOf(noticed)) {
                    // calm -> suspicious
                    monitor.setSuspicious(noticed);
                }
            }

            @Override
            protected float getAwareness() {
                // low amount of wandering and reduced field of view
                return 0.75f;
            }
        },

        /**
         * Suspicious NPCs will pursue any potential enemies they encounter, and attack once they
         * get close enough to confirm their suspicions.  Suspicious NPCs will also wander more and
         * idle less.
         */
        Suspicious() {
            @Override
            public void enter(Npc entity) {
                reset(entity);
            }

            @Override
            public void afterUpdate(Npc entity) {
                NpcThreatMonitor monitor = entity.getThreat();
                monitor.suspicion.update(DELTA);

                // in order to go from suspicious to calm, we must meet the following criteria:
                // 1. suspicion timer is finished
                // 2. all assaulters are dead
                // 3. the location is not actively under alert
                if (monitor.suspicion.isFinished()) {
                    // suspicious -> calm
                    monitor.setCalm();
                }
            }

            @Override
            protected void notice(Npc npc, Agent noticed) {
                NpcThreatMonitor monitor = npc.getThreat();
                if (monitor.isAlertedTo(noticed)) {
                    // suspicious -> alerted
                    monitor.setAlerted(noticed);
                } else {
                    // continue being suspicious
                    reset(npc);
                }
            }

            private void reset(Npc entity) {
                entity.getThreat().suspicion.reset(SUSPICION_SECS);
            }
            
            @Override
            protected float getAwareness() {
                // standard amount of wandering
                return 1f;
            }
        },

        /**
         * Where the NPC is alerted, they will shoot any enemy they see on sight.  Alerted NPCs do
         * not idle.
         */
        Alerted() {
            @Override
            public void enter(Npc entity) {
                reset(entity);
            }

            @Override
            public void afterUpdate(Npc entity) {
                NpcThreatMonitor monitor = entity.getThreat();
                monitor.alert.update(DELTA);

                // in order to go from alerted to suspicious, we must meet the following criteria:
                // 1. alert timer is finished
                // 2. has no enemies
                if (monitor.alert.isFinished() && !monitor.hasEnemies()) {
                    // alerted -> suspicious
                    monitor.setSuspicious();
                }
            }

            @Override
            protected void notice(Npc npc, Agent noticed) {
                // alerted NPCs shoot on sight
                NpcThreatMonitor monitor = npc.getThreat();
                monitor.addEnemy(noticed);
                reset(npc);
            }

            private void reset(Npc entity) {
                entity.getThreat().alert.reset(ALERT_SECS);
            }
            
            @Override
            protected float getAwareness() {
                // no idling
                return 1f;
            }
        };
        
        @Override
        public void update(Npc entity) {
            for (Agent neighbor : entity.getVisibleNeighbors()) {
                if (entity.getThreat().hasEnemy(neighbor)) {
                    notice(entity, neighbor);
                }
            }
            afterUpdate(entity);
        }

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
        
        protected abstract void afterUpdate(Npc npc);

        protected abstract void notice(Npc npc, Agent noticed);
        
        protected abstract float getAwareness();
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
