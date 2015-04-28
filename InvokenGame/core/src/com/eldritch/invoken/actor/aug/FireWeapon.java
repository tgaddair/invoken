package com.eldritch.invoken.actor.aug;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.effects.HoldingWeapon;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.screens.GameScreen;

public class FireWeapon extends ProjectileAugmentation {
    private static final float ALERT_RADIUS = 10;

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
    public boolean isValid(Agent owner) {
        return super.isValid(owner) && owner.getInventory().canUseRangedWeapon();
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return super.isValid(owner, target) && isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return super.isValid(owner, target) && isValid(owner);
    }

    @Override
    public int getCost(Agent owner) {
        if (!owner.getInventory().hasRangedWeapon()) {
            return 0;
        }
        return owner.getInventory().getRangedWeapon().getBaseCost();
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        if (!owner.getInventory().hasRangedWeapon() || !target.isAlive()) {
            return 0;
        }
        
        float idealDst = owner.getInventory().getRangedWeapon().getIdealDistance();
        float delta = Math.abs(owner.dst2(target) - idealDst * idealDst);
        return 10 / delta;
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
            // add projectile to scene
            RangedWeapon weapon = owner.getInventory().getRangedWeapon();
            for (HandledProjectile projectile : weapon.getProjectiles(owner)) {
                location.addEntity(projectile);
            }

            // update agent to fact the direction of their shots
            owner.setDirection(owner.getRelativeDirection(target));

            // add camera shake
            owner.recoil();

            // add cooldown to weapon
            owner.getInventory().setCooldown(weapon, weapon.getCooldown());

            // alert all enemies in range if the weapon is not silenced
            for (Agent neighbor : owner.getNeighbors()) {
                if (owner.dst2(neighbor) < ALERT_RADIUS * ALERT_RADIUS) {
                    neighbor.alertTo(owner);
                }
            }

            // play sound effect
            InvokenGame.SOUND_MANAGER.playAtPoint(weapon.getSoundEffect(), owner.getPosition());
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }

        private Animation getAnimation() {
            return animations.get(owner.getDirection());
        }
    }
}
