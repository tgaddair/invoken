package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.DamageHandler;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.effects.Detonation;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;

public class ExplosiveCannister extends CollisionActivator {
    private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
            "sprite/effects/blue-explosion.png", 256, 256);

    private static final float RANGE = 2f;
    private static final int BASE_DAMAGE = 100;

    public ExplosiveCannister(NaturalVector2 position) {
        super(position);
    }

    @Override
    protected AgentHandler getCollisionHandler(InanimateEntity entity) {
        return new ExplosiveCannisterHandler(entity);
    }

    private class ExplosiveCannisterHandler extends DamageHandler {
        private final InanimateEntity entity;

        public ExplosiveCannisterHandler(InanimateEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean handle(Damager damager) {
            Level level = getLevel();
            Damage damage = Damage.from(level.getPlayer(), DamageType.PHYSICAL, BASE_DAMAGE);
            Detonation detonation = new Detonation(damage, entity.getPosition().cpy(), RANGE,
                    explosionRegions);
            level.addEntity(detonation);

            finish();
            return true;
        }
    }
}
