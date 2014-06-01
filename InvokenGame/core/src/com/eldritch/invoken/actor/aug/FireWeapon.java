package com.eldritch.invoken.actor.aug;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.Agent.Direction;
import com.eldritch.invoken.actor.Entity;
import com.eldritch.invoken.actor.TemporaryEntity;
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

    public static class Bullet implements TemporaryEntity {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet-blue.png"));
        private final Vector2 position = new Vector2();
        private final Vector2 velocity = new Vector2();
        private Agent owner;
        private boolean finished;

        public void setup(Agent source, Agent target) {
            owner = source;
            finished = false;
            position.set(source.getForwardVector().scl(0.5f).add(source.getPosition()));
            velocity.set(target.getPosition());
            velocity.sub(source.getPosition());
            velocity.nor();
        }

        @Override
        public void update(float delta, Location location) {
            float scale = 15 * delta;
            velocity.scl(scale);
            position.add(velocity);
            velocity.scl(1 / scale);

            for (Agent agent : location.getActors()) {
                if (agent.getPosition().dst2(
                        position.x + velocity.x * 0.5f,
                        position.y + velocity.y * 0.5f) < 0.1) {
                    apply(agent);
                }
            }
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            Batch batch = renderer.getSpriteBatch();
            batch.begin();
            batch.draw(texture, position.x - 0.5f, position.y - 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f,
                    velocity.angle());
            batch.end();
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        private void apply(Agent target) {
            target.addEffect(new Bleed(owner, target, 5));
            finished = true;
            bulletPool.free(this);
        }
    }
}
