package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Applier;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.box2d.AreaOfEffect;
import com.eldritch.invoken.box2d.AreaOfEffect.AoeHandler;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.util.Damage;

public class Rending extends BasicEffect {
    private final Vector2 position = new Vector2();
    private final Applier applier;
    private final Damage damage;
    private final float radius;

    private AreaOfEffect aoe;
    float elapsed = 0;

    public Rending(Agent target, Vector2 position, Applier applier, Damage damage, float radius) {
        super(target);
        this.position.set(position);
        this.applier = applier;
        this.damage = damage;
        this.radius = radius;
    }

    @Override
    protected void doApply() {
        this.aoe = target.getLocation().obtainAoe(new RendHandler(position, damage, radius));
    }

    @Override
    public void dispel() {
        target.getLocation().freeAoe(aoe);
    }

    @Override
    public boolean isFinished() {
        return elapsed > 0;
    }

    @Override
    protected void update(float delta) {
        elapsed += delta;
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
            
            applier.apply(agent);
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
