package com.eldritch.invoken.actor.type;

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
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Constants;
import com.eldritch.invoken.util.Settings;

public class InanimateEntity extends CollisionEntity implements Drawable, Registered {
    private final TiledMapTileLayer layer;
    private final BodyType bodyType;
    
    private TiledMapTileLayer collisionLayer;
    private Vector2 offset;
    private Body body;
    private float radius = 0;

    public InanimateEntity(TiledMapTileLayer layer, NaturalVector2 position, BodyType bodyType) {
        super(getWidth(layer), getHeight(layer));
        this.layer = layer;
        this.offset = Vector2.Zero;
        this.bodyType = bodyType;
        this.position.set(position.x, position.y);
    }
    
    public void addCollisionLayer(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
        this.offset = getOffset(collisionLayer);
        this.radius = getWidth(collisionLayer) / 3;
    }

    @Override
    public void register(Location location) {
        if (radius > 0) {
            body = createBody(location.getWorld());
        }
    }

    @Override
    public void update(float delta, Location location) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getBatch();
        batch.begin();
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                Cell cell = layer.getCell(i, j);
                if (cell != null && cell.getTile() != null) {
                    Vector2 position = getBodyPosition();
                    batch.draw(cell.getTile().getTextureRegion(), position.x + i
                            - offset.x - 0.5f, position.y + j - offset.y - 0.5f, 1, 1);
                }
            }
        }
        batch.end();
    }
    
    public Vector2 getBodyPosition() {
        return body != null ? body.getPosition() : getPosition();
    }

    @Override
    public float getZ() {
        return getBodyPosition().y;
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
        filter.categoryBits = Settings.BIT_AGENT;
        filter.maskBits = Settings.BIT_ANYTHING;
        
        // optional collision filter
        MapProperties props = collisionLayer.getProperties();
        if (props.containsKey("category")) {
            if (props.get("category").equals("low")) {
                filter.categoryBits = Settings.BIT_SHORT_OBSTACLE;
            }
        }
        
        fixture.setFilterData(filter);

        body.setAngularDamping(10);
        body.setLinearDamping(10);

        circleShape.dispose();
        return body;
    }

    private float getRadius() {
        return radius;
    }

    private static float getWidth(TiledMapTileLayer layer) {
        float maxWidth = 0;
        for (int j = 0; j < layer.getHeight(); j++) {
            int width = 0;
            for (int i = 0; i < layer.getWidth(); i++) {
                if (isCollider(layer, i, j)) {
                    width++;
                }
            }
            maxWidth = Math.max(maxWidth, width);
        }
//        System.out.println("width = " + maxWidth);
        return maxWidth;
    }

    private static float getHeight(TiledMapTileLayer layer) {
        float maxHeight = 0;
        for (int i = 0; i < layer.getWidth(); i++) {
            int height = 0;
            for (int j = 0; j < layer.getHeight(); j++) {
                if (isCollider(layer, i, j)) {
                    height++;
                }
            }
            maxHeight = Math.max(maxHeight, height);
        }
        System.out.println("height = " + maxHeight);
        return maxHeight;
    }

    private static Vector2 getOffset(TiledMapTileLayer layer) {
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                if (isCollider(layer, i, j)) {
                    System.out.println("offset = " + i + " " + j);
                    return new Vector2(i, j);
                }
            }
        }
        return Vector2.Zero;
    }
    
    private static boolean isCollider(TiledMapTileLayer layer, int x, int y) {
        Cell cell = layer.getCell(x, y);
        if (cell == null) {
            return false;
        }
        
        TiledMapTile tile = cell.getTile();
        if (tile == null) {
            return false;
        }
        
        MapProperties props = tile.getProperties();
        return props.containsKey(Constants.TRANSIENT);
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
}
