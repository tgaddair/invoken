package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.box2d.AreaOfEffect;
import com.eldritch.invoken.box2d.AreaOfEffect.AoeHandler;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Rending extends BasicEffect {
    private final Vector2 position = new Vector2();
    private final Damage damage;
    private final float radius;

    private AreaOfEffect aoe;
    private boolean applied = false;

    public Rending(Agent target, Vector2 position, Damage damage, float radius) {
        super(target);
        this.position.set(position);
        this.damage = damage;
        this.radius = radius;
    }

    @Override
    protected void doApply() {
        this.aoe = new AreaOfEffect(target.getLocation().getWorld());
        aoe.setup(new RendHandler(position, damage, radius));
        applied = true;
    }

    @Override
    public void dispel() {
        target.getLocation().getWorld().destroyBody(aoe.getBody());
    }

    @Override
    public boolean isFinished() {
        return applied;
    }

    @Override
    protected void update(float delta) {
    }

    public class RendHandler extends DefaultAgentHandler implements AoeHandler {
        private final Vector2 center;
        private final Damage damage;
        private final float radius;

        public RendHandler(Vector2 center, Damage damage, float radius) {
            this.center = center;
            this.damage = damage;
            this.radius = radius;
        }

        @Override
        public boolean handle(Agent agent) {
            if (agent == getTarget()) {
                // cannot hit ourselves
                return false;
            }
            
            agent.addEffect(new Bleed(agent, damage));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_HIT, agent.getPosition());
            return true;
        }

        @Override
        public boolean handle(Object userData) {
            if (userData instanceof DamageHandler) {
                DamageHandler handler = (DamageHandler) userData;
                handler.handle(this);
                return true;
            }
            return false;
        }

        @Override
        public Damage getDamage() {
            return damage;
        }

        @Override
        public Vector2 getCenter() {
            return center;
        }

        @Override
        public float getRadius() {
            return radius;
        }
    }
}
