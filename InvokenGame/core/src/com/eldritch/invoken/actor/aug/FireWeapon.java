package com.eldritch.invoken.actor.aug;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;
import com.eldritch.invoken.actor.Projectile;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class FireWeapon extends Augmentation {
    private static Pool<Bullet> bulletPool = new Pool<Bullet>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };

    public FireWeapon() {
        super("fire");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new FireAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && owner.hasWeapon();
    }

    public class FireAction extends AnimatedAction {
        private final Agent target;
        private final float width;
        private final float height;
        private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        public FireAction(Agent actor, Agent target) {
            super(actor, Activity.Combat);
            this.target = target;

            TextureRegion[][] regions = GameScreen.getRegions("sprite/effects/muzzle-flash.png",
                    48, 48);
            for (Direction d : Direction.values()) {
                Animation anim = new Animation(0.05f, regions[d.ordinal()]);
                anim.setPlayMode(Animation.PlayMode.NORMAL);
                animations.put(d, anim);
            }

            width = 1 / 32f * regions[0][0].getRegionWidth();
            height = 1 / 32f * regions[0][0].getRegionHeight();
        }

        @Override
        public void render(OrthogonalTiledMapRenderer renderer) {
            super.render(renderer);

            TextureRegion frame = getAnimation().getKeyFrame(stateTime);
            Vector2 position = owner.getPosition();

            Batch batch = renderer.getSpriteBatch();
            batch.begin();
            batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
            batch.end();

            // render weapon
            owner.getWeapon().render(owner, Activity.Combat, getStateTime(), renderer);
        }

        @Override
        public boolean isFinished() {
            return getAnimation().isAnimationFinished(stateTime);
        }

        @Override
        public void apply(Location location) {
            // add bullet to scene
            Bullet bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
        }

        private Animation getAnimation() {
            return animations.get(owner.getDirection());
        }
    }

    public static class Bullet extends Projectile {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet-blue.png"));
        
        public Bullet() {
            super(1, 1, 15);
        }
        
        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Bleed(owner, target, 5));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return texture;
        }

        @Override
        protected void free() {
            bulletPool.free(this);
        }
    }
}
