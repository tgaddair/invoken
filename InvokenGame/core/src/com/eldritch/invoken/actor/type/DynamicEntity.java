package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
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
import com.eldritch.invoken.actor.Locatable;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.util.Settings;

public class DynamicEntity extends CollisionEntity implements Drawable, Locatable {
    private final TiledMapTileLayer layer;
    private final Vector2 offset;
    private Body body;

    public DynamicEntity(TiledMapTileLayer layer, NaturalVector2 position) {
        super(getWidth(layer), getHeight(layer));
        this.layer = layer;
        this.offset = getOffset(layer);
        this.position.set(position.x, position.y);
    }

    @Override
    public void register(Location location) {
        body = createBody(location.getWorld());
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
                    batch.draw(cell.getTile().getTextureRegion(), body.getPosition().x + i
                            - offset.x - 0.5f, body.getPosition().y + j - offset.y - 0.5f, 1, 1);
                }
            }
        }
        batch.end();
    }

    @Override
    public float getZ() {
        return body.getPosition().y;
    }

    private Body createBody(World world) {
        CircleShape circleShape = new CircleShape();
        circleShape.setPosition(new Vector2());
        circleShape.setRadius(getRadius());

        BodyDef characterBodyDef = new BodyDef();
        characterBodyDef.position.set(getPosition().x + offset.x + getRadius(), getPosition().y
                + offset.y + getRadius());
        characterBodyDef.type = BodyType.DynamicBody;
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
        MapProperties props = layer.getProperties();
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
        return getWidth() / 3;
    }

    private static float getWidth(TiledMapTileLayer layer) {
        float maxWidth = 0;
        for (int j = 0; j < layer.getHeight(); j++) {
            int width = 0;
            for (int i = 0; i < layer.getWidth(); i++) {
                if (layer.getCell(i, j) != null) {
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
                if (layer.getCell(i, j) != null) {
                    height++;
                }
            }
            maxHeight = Math.max(maxHeight, height);
        }
//        System.out.println("height = " + maxHeight);
        return maxHeight;
    }

    private static Vector2 getOffset(TiledMapTileLayer layer) {
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                if (layer.getCell(i, j) != null) {
//                    System.out.println("offset = " + i + " " + j);
                    return new Vector2(i, j);
                }
            }
        }
        return Vector2.Zero;
    }
}
