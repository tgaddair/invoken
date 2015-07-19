package com.eldritch.invoken.actor.type;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.eldritch.invoken.actor.Drawable;
import com.eldritch.invoken.actor.Registered;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.LightDescription;
import com.eldritch.invoken.gfx.Light.OwnedLight;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Constants;
import com.eldritch.invoken.util.Settings;

public abstract class InanimateEntity extends CollisionEntity implements Drawable, Registered {
    private final List<InanimateEntityListener> listeners = new ArrayList<>();
    private final List<Light> lights = new ArrayList<>();
    private final TiledMapTileLayer layer;
    private final BodyType bodyType;
    private final float zOff;

    private TiledMapTileLayer collisionLayer;
    private Vector2 offset;
    private Body body;
    private float radius = 0;
    private boolean finished = false;

    public InanimateEntity(TiledMapTileLayer layer, NaturalVector2 position, BodyType bodyType) {
        super(position.toVector2(), getWidth(layer, false), getHeight(layer, false));
        this.layer = layer;
        this.offset = Vector2.Zero;
        this.zOff = getOffset(layer, false).y;
        this.bodyType = bodyType;
    }
    
    public Body getBody() {
        return body;
    }
    
    public void addListener(InanimateEntityListener listener) {
        listeners.add(listener);
    }

    public void addCollisionLayer(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
        this.radius = getWidth(collisionLayer, true) / 3;
        if (radius > 0) {
            this.offset = getOffset(collisionLayer, true).add(0.5f, 0.5f); // centered
        }
    }
    
    public void addLight(LightDescription description) {
        Vector2 lightOffset = new Vector2();
        description.getBounds().getCenter(lightOffset);
        lightOffset.sub(offset);
        lights.add(new OwnedLight(this, description, lightOffset));
    }

    @Override
    public void register(Level level) {
        if (radius > 0) {
            body = createBody(level.getWorld());
            for (InanimateEntityListener listener : listeners) {
                listener.onBodyCreation(this, body);
            }
        }
        level.addLights(lights);
    }

    @Override
    public void update(float delta, Level level) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getBatch();
        batch.begin();
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                Cell cell = layer.getCell(i, j);
                if (cell != null && cell.getTile() != null) {
                    Vector2 position = getPosition();
                    batch.draw(cell.getTile().getTextureRegion(), position.x + i - offset.x,
                            position.y + j - offset.y, 1, 1);
                }
            }
        }
        batch.end();
    }

    @Override
    public Vector2 getPosition() {
        return body != null ? body.getPosition() : super.getPosition();
    }

    @Override
    public float getZ() {
        return body != null ? body.getPosition().y : super.getPosition().y + zOff;
    }
    
    public void finish() {
        this.finished = true;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public void dispose(Level level) {
        level.getWorld().destroyBody(body);
    }

    private Body createBody(World world) {
        CircleShape circleShape = new CircleShape();
        circleShape.setPosition(new Vector2());
        circleShape.setRadius(getRadius());

        BodyDef characterBodyDef = new BodyDef();
        characterBodyDef.position.set(getPosition().x + offset.x + getRadius(), getPosition().y
                + offset.y + getRadius());
        characterBodyDef.type = bodyType;
        Body body = world.createBody(characterBodyDef);

        FixtureDef charFixtureDef = new FixtureDef();
        charFixtureDef.shape = circleShape;
        charFixtureDef.filter.groupIndex = 0;

        Fixture fixture = body.createFixture(charFixtureDef);
        fixture.setUserData(this); // allow callbacks to owning Agent

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = Settings.BIT_HIGH_AGENT;
        filter.maskBits = Settings.BIT_ANYTHING;

        // optional collision filter
        updateCategory(layer, filter);
        updateCategory(collisionLayer, filter);

        fixture.setFilterData(filter);

        body.setAngularDamping(10);
        body.setLinearDamping(10);

        circleShape.dispose();
        return body;
    }
    
    private void updateCategory(TiledMapTileLayer layer, Filter filter) {
        MapProperties props = layer.getProperties();
        if (props.containsKey(Constants.CATEGORY)) {
            if (props.get(Constants.CATEGORY).equals(Constants.LOW)) {
                filter.categoryBits = Settings.BIT_SHORT_OBSTACLE;
            }
        }
    }

    private float getRadius() {
        return radius;
    }

    private static float getWidth(TiledMapTileLayer layer, boolean checkTransient) {
        float maxWidth = 0;
        for (int j = 0; j < layer.getHeight(); j++) {
            int width = 0;
            for (int i = 0; i < layer.getWidth(); i++) {
                if (isCollider(layer, i, j, checkTransient)) {
                    width++;
                }
            }
            maxWidth = Math.max(maxWidth, width);
        }
        // System.out.println("width = " + maxWidth);
        return maxWidth;
    }

    private static float getHeight(TiledMapTileLayer layer, boolean checkTransient) {
        float maxHeight = 0;
        for (int i = 0; i < layer.getWidth(); i++) {
            int height = 0;
            for (int j = 0; j < layer.getHeight(); j++) {
                if (isCollider(layer, i, j, checkTransient)) {
                    height++;
                }
            }
            maxHeight = Math.max(maxHeight, height);
        }
        // System.out.println("height = " + maxHeight);
        return maxHeight;
    }

    private static Vector2 getOffset(TiledMapTileLayer layer, boolean checkTransient) {
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                if (isCollider(layer, i, j, checkTransient)) {
                    // System.out.println("offset = " + i + " " + j);
                    return new Vector2(i, j);
                }
            }
        }
        return Vector2.Zero.cpy();
    }

    private static boolean isCollider(TiledMapTileLayer layer, int x, int y, boolean checkTransient) {
        Cell cell = layer.getCell(x, y);
        if (cell == null) {
            return false;
        }
        
        if (cell.getTile() != null && cell.getTile().getProperties().containsKey(Constants.BLANK)) {
            // blank tile, no collisions
            return false;
        }

        if (checkTransient) {
            TiledMapTile tile = cell.getTile();
            if (tile == null) {
                return false;
            }

            MapProperties props = tile.getProperties();
            return props.containsKey(Constants.TRANSIENT);
        }
        return true;
    }
    
    public static class DynamicEntity extends InanimateEntity {
        public DynamicEntity(TiledMapTileLayer layer, NaturalVector2 position) {
            super(layer, position, BodyType.DynamicBody);
        }
    }

    public static class StaticEntity extends InanimateEntity {
        public StaticEntity(TiledMapTileLayer layer, NaturalVector2 position) {
            super(layer, position, BodyType.StaticBody);
        }
    }
    
    public interface InanimateEntityListener {
        void onBodyCreation(InanimateEntity entity, Body body);
    }
}
