package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.effects.Detonation;
import com.eldritch.invoken.location.Bullet;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Settings;

public class ExplosiveCannister extends CollisionActivator {
    private static final float RANGE = 1.5f;
    private static final int BASE_DAMAGE = 100;

    public ExplosiveCannister(NaturalVector2 position) {
        super(position);
    }

    @Override
    protected AgentHandler getCollisionHandler(InanimateEntity entity) {
        return new BulletHandler(entity);
    }

    private class BulletHandler implements AgentHandler {
        private final InanimateEntity entity;

        public BulletHandler(InanimateEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean handle(Agent agent) {
            return false;
        }

        @Override
        public boolean handle(Object userData) {
            if (userData instanceof Bullet) {
                Level level = getLevel();
                Damage damage = Damage.from(level.getPlayer(), DamageType.PHYSICAL, BASE_DAMAGE);
                Detonation detonation = new Detonation(damage, entity.getPosition().cpy(), RANGE);
                level.addEntity(detonation);

                finish();
                return true;
            }
            return false;
        }

        @Override
        public short getCollisionMask() {
            return Settings.BIT_ANYTHING;
        }
    }
}
