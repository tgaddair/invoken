package com.eldritch.invoken.effects;

import java.util.List;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.FireWeapon;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.google.common.collect.ImmutableList;

/**
 * Produces bullets at a timed interval, or based on some other condition, until cancelled.
 */
public abstract class ProjectileSpawn extends BasicEffect {
    private static final float ALERT_RADIUS = 10;

    public ProjectileSpawn(Agent target) {
        super(target);
    }

    @Override
    public void dispel() {
    }

    @Override
    protected void update(float delta) {
    }

    protected final void doSpawn(HandledProjectile projectile) {
        target.getLocation().addEntity(projectile);
        target.getInventory().useAmmunition(1);
    }

    protected final void afterSpawn() {
        // add camera shake
        target.recoil();

        // add cooldown to weapon
        RangedWeapon weapon = target.getInventory().getRangedWeapon();
        // target.getInventory().setCooldown(weapon, weapon.getCooldown());

        // alert all enemies in range if the weapon is not silenced
        for (Agent neighbor : target.getNeighbors()) {
            if (target.dst2(neighbor) < ALERT_RADIUS * ALERT_RADIUS) {
                neighbor.suspicionTo(target);
            }
        }

        // play sound effect
        InvokenGame.SOUND_MANAGER.playAtPoint(weapon.getSoundEffect(), target.getPosition());
    }

    public static class SingleProjectileSpawn extends FixedProjectileSpawn {
        public SingleProjectileSpawn(Agent target, HandledProjectile bullet) {
            super(target, ImmutableList.of(bullet));
        }
    }

    public static class FixedProjectileSpawn extends ProjectileSpawn {
        private final List<HandledProjectile> bullets;
        private boolean applied = false;

        public FixedProjectileSpawn(Agent target, List<HandledProjectile> bullets) {
            super(target);
            this.bullets = bullets;
        }

        @Override
        protected void doApply() {
            // add projectiles to scene, all bullets together cost 1 unit of ammo
            for (HandledProjectile projectile : bullets) {
                target.getLocation().addEntity(projectile);
            }
            target.getInventory().useAmmunition(1);

            afterSpawn();
            applied = true;
        }

        @Override
        public boolean isFinished() {
            return applied;
        }
    }

    public static class TimedProjectileSpawn extends ProjectileSpawn {
        public TimedProjectileSpawn(Agent target, ProjectileGenerator generator) {
            super(target);
        }

        @Override
        protected void doApply() {
            afterSpawn();
        }

        @Override
        public boolean isFinished() {
            return !target.isToggled(FireWeapon.class);
        }
    }

    public static class DelayedProjectileSpawn extends ProjectileSpawn {
        private final ProjectileGenerator generator;
        private final int limit;
        private final float delay;

        private int count = 0;
        private float elapsed = 0;

        public DelayedProjectileSpawn(Agent target, ProjectileGenerator generator, int limit,
                float delay) {
            super(target);
            this.generator = generator;
            this.limit = limit;
            this.delay = delay;
        }

        @Override
        protected void doApply() {
            // fire one bullet on apply
            spawn();
        }

        @Override
        protected void update(float delta) {
            elapsed += delta;
            if (elapsed > delay) {
                // fire a bullet after every delay has elapsed
                spawn();
                elapsed = 0;
            }
        }

        @Override
        public boolean isFinished() {
            return count >= limit;
        }

        private void spawn() {
            if (count < limit) {
                HandledProjectile bullet = generator.generate(target);
                doSpawn(bullet);
                count++;
                afterSpawn();
            }
        }
    }

    public static interface ProjectileGenerator {
        HandledProjectile generate(Agent owner);
    }
}
