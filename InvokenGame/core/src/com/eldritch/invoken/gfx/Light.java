package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.util.Locatable;

public abstract class Light {
    private final float magnitude;
    private final Color color;
    private boolean oscillate;
    private Texture light;
    private float zAngle;

    public Light(float magnitude, boolean oscillate) {
        this.magnitude = magnitude;
        this.oscillate = oscillate;
        this.color = new Color(0.9f, 0.4f, 0.2f, NormalMapShader.DEFAULT_LIGHT_INTENSITY);
        light = new Texture("light/light3.png");
    }

    public Light(LightDescription description) {
        this.magnitude = description.getMagnitude();
        this.oscillate = description.getOscillate();
        this.color = description.getColor();
        light = new Texture("light/light3.png");
    }
    
    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    public Color getColor() {
        return color;
    }

    public void update(float zAngle) {
        this.zAngle = zAngle;
    }

    public void render(Batch batch, float zAngle) {
        float lightSize = getRadius();
        Vector2 position = getPosition();
        batch.draw(light, position.x - lightSize * 0.5f, position.y - lightSize * 0.5f, lightSize,
                lightSize);
    }

    public void bind(int unit) {
        light.bind(unit);
    }

    public float getRadius() {
        return oscillate ? (magnitude * 0.75f + 0.25f * (float) Math.sin(zAngle) + .2f * MathUtils
                .random()) : magnitude;
    }

    public abstract Vector2 getPosition();

    public abstract boolean isDynamic();

    public static class OwnedLight extends Light {
        private final Locatable owner;
        private final Vector2 position = new Vector2();
        private final Vector2 offset = new Vector2();

        public OwnedLight(Locatable owner) {
            super(5, false);
            this.owner = owner;
        }

        public OwnedLight(Locatable owner, LightDescription description, Vector2 offset) {
            super(description);
            this.owner = owner;
            this.offset.set(offset);
        }

        @Override
        public Vector2 getPosition() {
            return position.set(owner.getPosition()).add(offset);
        }

        @Override
        public boolean isDynamic() {
            return true;
        }
    }

    public static class StaticLight extends Light {
        private final Vector2 position;

        public StaticLight(Vector2 position) {
            this(position, 5, Math.random() < 0.2);
        }
        
        public StaticLight(Vector2 position, float magnitude, boolean oscillate) {
            super(magnitude, oscillate);
            this.position = position;
        }

        public StaticLight(Vector2 position, LightDescription description) {
            super(description);
            this.position = position;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public boolean isDynamic() {
            return false;
        }
    }

    public static class LightDescription {
        private final Rectangle bounds;
        private final Color color;
        private final float magnitude;
        private final boolean oscillate = false;
        
        public LightDescription(Rectangle bounds, Color color, float magnitude) {
            this.bounds = bounds;
            this.color = color;
            this.magnitude = magnitude;
        }

        public float getMagnitude() {
            return magnitude;
        }

        public boolean getOscillate() {
            return oscillate;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public Color getColor() {
            return color;
        }
        
        public static LightDescription from(TiledMapTileLayer layer) {
            Rectangle bounds = getBounds(layer);
            Color color = getColor(layer);
            float magnitude = getMagnitude(layer, bounds);
            return new LightDescription(bounds, color, magnitude);
        }
        
        private static float getMagnitude(TiledMapTileLayer layer, Rectangle bounds) {
            MapProperties props = layer.getProperties();
            if (props.containsKey("magnitude")) {
                return Float.parseFloat((String) props.get("magnitude"));
            }
            
            return 2 + bounds.area();
        }
        
        private static Rectangle getBounds(TiledMapTileLayer layer) {
            boolean origin = false;
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;

            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        if (!origin) {
                            startX = x;
                            startY = y;
                            origin = true;
                        }
                        endX = x;
                        endY = y;
                    }
                }
            }

            return new Rectangle(startX, startY, endX - startX + 1, endY - startY + 1);
        }

        private static Color getColor(TiledMapTileLayer layer) {
            MapProperties props = layer.getProperties();
            Color color = new Color();
            color.r = props.containsKey("r") ? Float.parseFloat((String) props.get("r")) : 1;
            color.g = props.containsKey("g") ? Float.parseFloat((String) props.get("g")) : 1;
            color.b = props.containsKey("b") ? Float.parseFloat((String) props.get("b")) : 1;
            color.a = props.containsKey("a") ? Float.parseFloat((String) props.get("a"))
                    : NormalMapShader.DEFAULT_LIGHT_INTENSITY;
            return color;
        }
    }
}
