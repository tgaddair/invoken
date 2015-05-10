package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent.WeaponSentry;
import com.eldritch.invoken.location.Bullet;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Settings;

public abstract class Projectile extends CollisionEntity implements AgentHandler, TemporaryEntity {
    private final Bullet bullet;
    private final float speed;
    private final Damage damage;
    private Agent owner;
    private boolean finished;
    private float stateTime;

    public Projectile(Agent owner, TextureRegion region, float speed, Damage damage) {
        this(owner, region.getRegionWidth() * Settings.SCALE, region.getRegionHeight() * Settings.SCALE,
                speed, damage);
    }

    public Projectile(Agent owner, float width, float height, float speed, Damage damage) {
        super(width, height);
        this.speed = speed;
        this.damage = damage;
        
        this.owner = owner;
        setup(owner);

        Location location = owner.getLocation();
        bullet = location.obtainBullet(this, position, velocity);
    }
    
    public Damage getDamage() {
        return damage;
    }

    @Override
    public boolean handle(Agent agent) {
        if (agent != owner) {
            handleAgentContact(agent);
            return true;
        }
        return false;
    }

    @Override
    public boolean handle(Object userData) {
        handleObstacleContact();
        return true;
    }
    
    @Override
    public short getCollisionMask() {
        return Settings.BIT_SHOOTABLE;
    }

    public float getSpeed() {
        return speed;
    }

    public Agent getOwner() {
        return owner;
    }
    
    public Bullet getBullet() {
        return bullet;
    }

    private void setup(Agent source) {
        finished = false;
        stateTime = 0;

        WeaponSentry sentry = source.getWeaponSentry();
        position.set(sentry.getPosition());
        owner = source;
        velocity.set(sentry.getDirection());
        velocity.scl(speed);
    }
    
    /**
     * Rotate velocity theta degrees.
     */
    protected void rotate(float theta) {
        bullet.setVelocity(bullet.getVelocity().rotate(theta));
    }

    public void reset(Agent source, Vector2 target) {
        owner = source;

        velocity.set(target);
        velocity.sub(bullet.getPosition());
        velocity.nor();
        velocity.scl(speed);
        bullet.setVelocity(velocity);
    }

    @Override
    public void update(float delta, Location location) {
        stateTime += delta;
        
        if (handleBeforeUpdate(delta, location)) {
            return;
        }
        
        // check that we've passed into the walls, possibly skipping over an edge
        position.set(bullet.getPosition());
        NaturalVector2 position = NaturalVector2.of(bullet.getPosition());
        if (location.isBulletWall(position)) {
            handleObstacleContact();
        }

        // finally, reclaim the projectile if it has been active for more than 10 seconds
        if (stateTime >= 10) {
            finish();
        }
    }

    public boolean handleBeforeUpdate(float delta, Location location) {
        return false;
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        float width = getWidth();
        float height = getHeight();

        Batch batch = renderer.getBatch();
        batch.begin();
        preRender(batch);
        batch.draw(getTexture(0), bullet.getPosition().x - width / 2, bullet.getPosition().y
                - height / 2, // position
                width / 2, height / 2, // origin
                width, height, // size
                1f, 1f, // scale
                bullet.getVelocity().angle());
        postRender(batch);
        batch.end();
    }

    protected void preRender(Batch batch) {
    }

    protected void postRender(Batch batch) {
    }

    public void cancel() {
        finish();
    }

    private void finish() {
        finished = true;
        onFinish();
    }
    
    protected void onFinish() {
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void dispose() {
        owner.getLocation().freeBullet(bullet);
    }

    protected abstract TextureRegion getTexture(float delta);

    protected abstract void handleAgentContact(Agent agent);

    protected abstract void handleObstacleContact();
}
