package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.actor.aug.Drain.DrainBullet;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Draining;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.screens.GameScreen;

public class ThrowGrenade extends Augmentation {
    public ThrowGrenade() {
        super("throw");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
    	return getAction(owner, target.getPosition());
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ThrowAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && owner.hasLineOfSight(target);
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return owner.hasLineOfSight(target);
    }
    
    @Override
    public int getCost(Agent owner) {
        return 1;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return 1;
    }

    public class ThrowAction extends AnimatedAction {
        private final Vector2 target;

        public ThrowAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, ThrowGrenade.this);
            this.target= target;
        }

        @Override
        public void apply(Location location) {
            // update agent to fact the direction of their strike
            owner.setDirection(owner.getRelativeDirection(target));
            
            Grenade bullet = bulletPool.obtain();
            bullet.setup(owner, target);
            location.addEntity(bullet);
            
            
        	for (Agent neighbor : owner.getNeighbors()) {
        		if (neighbor.inRange(target, 10)) {
        			neighbor.addEffect(new Bleed(owner, neighbor, 10));
        		}
        	}
        }
        
        @Override
        public Vector2 getPosition() {
            return target;
        }
    }
    
    public static class Grenade extends HandledProjectile {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private final Animation animation;

        public Grenade() {
            super(1 / 32f * regions[0].getRegionWidth(), 1 / 32f * regions[0].getRegionWidth(), 10,
                    0);

            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Draining(owner, target, 5, 2));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return animation.getKeyFrame(stateTime);
        }

        @Override
        protected void free() {
            bulletPool.free(this);
        }
    }

    private static Pool<Grenade> bulletPool = new Pool<Grenade>() {
        @Override
        protected Grenade newObject() {
            return new Grenade();
        }
    };
}
