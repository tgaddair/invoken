package com.eldritch.invoken.actor.util;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public interface DodgeHandler {
    void update(float delta);
    
    boolean canDodge();
    
    void dodge(Vector2 direction);
    
    boolean isFinished();
    
    void onDodgeComplete();
    
    public abstract static class AbstractDodgeHandler implements DodgeHandler {
        private final Vector2 tmp = new Vector2();
        private final Agent agent;
        private final float scale;
        private final float cost;
        
        private boolean dodging = false;
        private float elapsed = 0;
        
        public AbstractDodgeHandler(Agent agent, float scale, float cost) {
            this.agent = agent;
            this.scale = scale;
            this.cost = cost;
        }
        
        @Override
        public void update(float delta) {
            if (dodging) {
                elapsed += delta;
                if (agent.getVelocity().len2() < 1) {
                    dodging = false;
                    elapsed = 0;
                    onDodgeComplete();
                } else if (elapsed > 1) {
                    dodging = false;
                    elapsed = 0;
                }
            }
        }
        
        @Override
        public boolean canDodge() {
            return agent.getInfo().getEnergy() > cost;
        }

        @Override
        public void dodge(Vector2 direction) {
            if (canDodge()) {
                float s = agent.getMaxLinearSpeed() / agent.getBaseSpeed();
                agent.applyForce(tmp.set(direction).scl(scale * s));
                agent.getInfo().expend(cost);
                InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.SWISH, agent.getPosition());
                dodging = true;
            }
        }
    }
    
    public static class DefaultDodgeHandler extends AbstractDodgeHandler {
        public static final float DODGE_SCALE = 500f;
        public static final float DODGE_COST = 10f;
        
        public DefaultDodgeHandler(Agent agent) {
            super(agent, DODGE_SCALE, DODGE_COST);
        }
        
        @Override
        public boolean isFinished() {
            // never remove
            return false;
        }

        @Override
        public void onDodgeComplete() {
        }
    }
}
