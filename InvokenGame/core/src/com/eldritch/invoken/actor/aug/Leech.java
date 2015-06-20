package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.effects.Draining;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Leech extends Augmentation {
    private static final float RANGE2 = 2;
    private static final float DURATION = 10f;
    private static final float MIN_V2 = 175f;
    private static final int DAMAGE = 25;

    private static class Holder {
        private static final Leech INSTANCE = new Leech();
    }

    public static Leech getInstance() {
        return Holder.INSTANCE;
    }

    private Leech() {
        super(false);
    }

    @Override
    public boolean isValid(Agent owner) {
        // only one at a time
        return !owner.isToggled(Leech.class);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new LeechAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new LeechAction(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive() || owner.dst2(target) >= RANGE2) {
            return 0;
        }

        return Heuristics.randomizedDistanceScore(owner.dst2(target), RANGE2);
    }

    public class LeechAction extends AnimatedAction {
        public LeechAction(Agent actor) {
            super(actor, Activity.Cast, Leech.this);
        }

        @Override
        public void apply(Level level) {
            owner.addEffect(new LeechEffect(owner));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.BUFF, owner.getPosition());
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    private static class LeechEffect extends BasicEffect {
        private final LeechHandler handler;
        private Joint joint = null;
        private Draining draining = null;
        private boolean cancelled = false;

        public LeechEffect(Agent agent) {
            super(agent);
            this.handler = new LeechHandler();
        }
        
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        protected void doApply() {
            target.toggleOn(Leech.class);
            target.setCollisionDelegate(handler);
        }

        @Override
        public void dispel() {
            target.toggleOff(Leech.class);
            target.removeCollisionDelegate();
            unweld();
        }

        @Override
        public boolean isFinished() {
            return getStateTime() > DURATION || cancelled;
        }

        @Override
        protected void update(float delta) {
            if (handler.victim != null && !cancelled) {
                if (joint == null) {
                    // weld the entities
                    weld(handler.victim);
                    
                    // start draining until detachment
                    Damage damage = Damage.from(target, DamageType.VIRAL, getBaseDamage(target));
                    this.draining = new Draining(handler.victim, damage, DURATION);
                    handler.victim.addEffect(draining);
                } else {
                    // check that the weld condition still holds
                    if (handler.victim.getBody().getLinearVelocity().len2() > MIN_V2) {
                        cancel();
                    }
                }
            }
        }

        private void weld(Agent victim) {
            WeldJointDef def = new WeldJointDef();

            def.collideConnected = false;
            Body body = target.getBody();
            Vector2 worldCoordsAnchorPoint = body.getWorldPoint(new Vector2(0.0f, 0.0f));

            def.bodyA = body;
            def.bodyB = victim.getBody();

            def.localAnchorA.set(def.bodyA.getLocalPoint(worldCoordsAnchorPoint));
            def.referenceAngle = def.bodyB.getAngle() - def.bodyA.getAngle();

            def.initialize(def.bodyA, def.bodyB, worldCoordsAnchorPoint);

            joint = target.getLocation().getWorld().createJoint(def);
        }
        
        private void unweld() {
            if (joint != null) {
                target.getLocation().getWorld().destroyJoint(joint);
                draining.cancel();
            }
        }
    }

    private static class LeechHandler implements AgentHandler {
        private Agent victim = null;

        @Override
        public boolean handle(Agent agent) {
            if (victim == null) {
                // only one leech
                victim = agent;
                return true;
            }
            return false;
        }

        @Override
        public boolean handle(Object userData) {
            return false;
        }
        
        @Override
        public short getCollisionMask() {
            return Settings.BIT_ANYTHING;
        }
    }
    
    private static int getBaseDamage(Agent owner) {
        return (int) (DAMAGE * owner.getInfo().getExecuteModifier());
    }
}
