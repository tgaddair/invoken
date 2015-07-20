package com.eldritch.invoken.activators.util;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.ProximityActivator;
import com.eldritch.invoken.activators.ProximityActivator.Indicator;
import com.eldritch.invoken.actor.items.Icepik;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.gfx.WorldText;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class LockManager {
    private final LockInfo lock;
    private final LockDamageHandler bulletHandler;
    private final LockCallback callback;
    
    public LockManager(LockInfo lock, LockCallback callback) {
        this.lock = lock;
        this.bulletHandler = new LockDamageHandler();
        this.callback = callback;
    }
    
    public void register(List<Body> bodies) {
        for (Body body : bodies) {
            for (Fixture fixture : body.getFixtureList()) {
                fixture.setUserData(bulletHandler);
            }
        }
    }
    
    public float getBaseHealth() {
        return bulletHandler.getBaseStrength();
    }
    
    public float getHealth() {
        return bulletHandler.getStrength();
    }
    
    public LockInfo getLock() {
        return lock;
    }
    
    public boolean isLocked() {
        return lock.isLocked();
    }
    
    public boolean isDamaged() {
        return bulletHandler.isDamaged();
    }
    
    public static class LockInfo {
        private final Optional<Item> key;
        private final ConnectedRoom room;
        private int strength;
        private boolean locked;
        private boolean broken = false;

        public LockInfo(String keyId, int strength, ConnectedRoom room) {
            boolean uniqueKey = false;
            if (!Strings.isNullOrEmpty(keyId)) {
                this.key = Optional.of(Item.fromProto(InvokenGame.ITEM_READER.readAsset(keyId)));
                uniqueKey = true;
            } else {
                this.key = Optional.of(room.getKey());
            }
            this.room = room;

            // strength key:
            // 0 -> open
            // 1 -> closed
            // 2+ -> locked
            // 10 -> requires key
            this.strength = strength;
            locked = uniqueKey || shouldLock();
        }
        
        public ConnectedRoom getRoom() {
            return room;
        }

        public Item getKey() {
            return key.get();
        }

        public void setStrength(int strength) {
            this.strength = strength;
            if (shouldLock()) {
                locked = true;
            }
        }

        public int getStrength() {
            return strength;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isLocked() {
            return locked;
        }

        public boolean canUnlock(Agent agent) {
            return hasKey(agent.getInventory());
        }

        public boolean hasKey(Inventory inventory) {
            return !key.isPresent() || inventory.hasItem(key.get());
        }

        private boolean shouldLock() {
            return strength > 1;
        }

        public boolean canPick(Agent agent) {
            int piks = getAvailablePiks(agent);
            int required = getRequiredPiks(agent);
            return piks >= required;
        }

        public int getAvailablePiks(Agent agent) {
            Icepik item = Icepik.from(agent.getLocation());
            if (agent.getInventory().hasItem(item)) {
                return agent.getInventory().getItemCount(item);
            }
            return 0;
        }

        public int getRequiredPiks(Agent agent) {
            int ability = agent.getInfo().getSubterfuge() / 10;
            int difficulty = strength;
            if (ability >= difficulty) {
                return 1;
            }

            // required piks scales with the square of the difference
            int delta = difficulty - ability;
            return delta * delta + 1;
        }

        public void breakLock() {
            broken = true;
        }

        public boolean isBroken() {
            return broken;
        }

        public static LockInfo from(ControlPoint controlPoint, ConnectedRoom room) {
            return new LockInfo(controlPoint.getRequiredKey(), controlPoint.getLockStrength(), room);
        }
    }
    
    private class LockDamageHandler extends DamageHandler {
        private static final float BASE_HEALTH = 100f;
        private float health = BASE_HEALTH;

        public boolean isDamaged() {
            return getStrength() < getBaseStrength();
        }

        public float getBaseStrength() {
            return BASE_HEALTH;
        }

        public float getStrength() {
            return health;
        }

        @Override
        public boolean handle(Damager damager) {
            Damage damage = damager.getDamage();
            health -= damage.getDamageOf(DamageType.PHYSICAL)
                    + damage.getDamageOf(DamageType.THERMAL);
            if (health <= 0) {
                callback.destroyBy(damage.getSource());
            }
            return true;
        }
    }
    
    public static class LockIndicator extends Indicator {
        private final WorldText lockText = new WorldText();
        private final LockInfo lock;

        public LockIndicator(Texture texture, Vector2 renderOffset, LockInfo lock) {
            super(texture, renderOffset);
            this.lock = lock;
        }

        @Override
        protected void preRender(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
            if (!lock.canPick(getLevel().getPlayer())) {
                renderer.getBatch().setColor(Color.RED);
                lockText.setColor(Color.RED);
            } else {
                lockText.setColor(Color.WHITE);
            }
        }

        @Override
        protected void postRender(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
            // draw icepik requirements
            Level level = getLevel();
            float x = getX(owner);
            float y = getY(owner);
            float w = getWidth();
            float h = getHeight();
            lockText.render(String.valueOf(lock.getRequiredPiks(level.getPlayer())),
                    level.getCamera(), x + w, y + h);
        }

        @Override
        protected boolean isActive(Level level, ProximityActivator owner) {
            return !lock.isBroken() && lock.isLocked() && super.isActive(level, owner);
        }
    }
    
    public interface LockCallback {
        void destroyBy(Agent source);
    }
}
