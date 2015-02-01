package com.eldritch.invoken.actor.aug;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.HoldingWeapon;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class FireWeapon extends ProjectileAugmentation {
    private static final float DAMAGE_SCALE = 5;
    private static final int BASE_COST = 10;
    
    private static class Holder {
        private static final FireWeapon INSTANCE = new FireWeapon();
	}
	
	public static FireWeapon getInstance() {
		return Holder.INSTANCE;
	}

    private FireWeapon() {
        super("fire");
    }
    
    @Override
    public void prepare(Agent owner) {
        // add an effect that shows a rotating weapon
        owner.toggleOn(HoldingWeapon.class);
        owner.addEffect(new HoldingWeapon(owner));
    }
    
    @Override
    public void unprepare(Agent owner) {
        owner.toggleOff(HoldingWeapon.class);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new FireAction(owner, target);
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return super.isValid(owner, target) && owner.getInventory().hasRangedWeapon();
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return super.isValid(owner, target) && owner.getInventory().hasRangedWeapon();
    }
    
    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }

    public class FireAction extends AnimatedAction {
        private final Vector2 target;
        private final float width;
        private final float height;
        private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();

        public FireAction(Agent actor, Vector2 target) {
            super(actor, Activity.Combat, FireWeapon.this);
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

            // draw the muzzle flash
            TextureRegion frame = getAnimation().getKeyFrame(stateTime);
            Vector2 position = owner.getRenderPosition();

            Batch batch = renderer.getSpriteBatch();
            batch.begin();
            batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
            batch.end();

            // render weapon
            owner.getInventory().getRangedWeapon()
                    .render(owner, Activity.Combat, getStateTime(), renderer);
        }

        @Override
        public boolean isFinished() {
            return getAnimation().isAnimationFinished(stateTime);
        }

        @Override
        protected boolean canApply() {
            Animation anim = getAnimation();
            return anim.getKeyFrameIndex(stateTime) == 2;
        }

        @Override
        public void apply(Location location) {
            // add bullet to scene
            Bullet bullet = bulletPool.obtain();
            bullet.setup(owner, owner.getWeaponSentry());
            location.addEntity(bullet);
            
            // update agent to fact the direction of their shots
            owner.setDirection(owner.getRelativeDirection(target));
        }
        
        @Override
        public Vector2 getPosition() {
            return target;
        }
        
        private Animation getAnimation() {
            return animations.get(owner.getDirection());
        }
    }

    public static class Bullet extends HandledProjectile {
        private static final TextureRegion texture = new TextureRegion(
                GameScreen.getTexture("sprite/effects/bullet1.png"));

        public Bullet() {
            super(texture, 40, 5);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
        	float magnitude = DAMAGE_SCALE * getDamage(target);
            target.applyForce(velocity.cpy().nor().scl(100));
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(owner, target, magnitude));
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
    
    private static Pool<Bullet> bulletPool = new Pool<Bullet>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };
}
