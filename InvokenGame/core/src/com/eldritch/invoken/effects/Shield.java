package com.eldritch.invoken.effects;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.HandledProjectile.ProjectileHandler;
import com.eldritch.invoken.util.AnimationUtils;

public class Shield extends BasicEffect {
    private static final float V_PENALTY = 5;
    private static final float ENERGY_COST = 20f;

    private final Augmentation aug;
    private final Map<Activity, Map<Direction, Animation>> animations = AnimationUtils
            .getHumanAnimations("sprite/effects/shield.png");
    private final ProjectileHandler handler = new ShieldProjectileHandler();

    public Shield(Agent actor, Augmentation aug) {
        super(actor);
        this.aug = aug;
    }

    @Override
    public void doApply() {
        target.addProjectileHandler(handler);
        target.getInfo().changeMaxEnergy(-ENERGY_COST);
        target.addVelocityPenalty(V_PENALTY); // shielding slows down the caster
    }

    @Override
    public void dispel() {
        target.removeProjectileHandler(handler);
        target.getInfo().getAugmentations().removeSelfAugmentation(aug);
        target.getInfo().changeMaxEnergy(ENERGY_COST);
        target.addVelocityPenalty(-V_PENALTY);
    }

    @Override
    public boolean isFinished() {
        return !getTarget().isToggled(Shield.class);
    }

    @Override
    protected void update(float delta) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Vector2 position = target.getRenderPosition();
        float width = target.getWidth();
        float height = target.getHeight();
        
        Animation animation = animations.get(target.getLastActivity()).get(target.getDirection());
        TextureRegion frame = animation.getKeyFrame(target.getLastStateTime());

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, // frame
                position.x - width / 2, position.y - height / 2, // position
                width / 2, height / 2, // origin
                width, height, // size
                1f, 1f, // scale
                0); // direction
        batch.end();
    }

    private class ShieldProjectileHandler implements ProjectileHandler {
        @Override
        public boolean handle(HandledProjectile projectile) {
            float damage = projectile.getDamage().get(target);
            if (damage > 0) {
                target.getInfo().expend(damage);
                if (target.getInfo().getEnergy() < damage) {
                    target.toggleOff(Shield.class);
                }

                projectile.cancel();
                return true;
            }
            return false;
        }
    }
}
