package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.util.Locatable;

public abstract class Light {
    private final float magnitude;
    private boolean oscillate;
    private Texture light;
    private float zAngle;
    
    public Light(float magnitude, boolean oscillate) {
        this.magnitude = magnitude;
        this.oscillate = oscillate;
        light = new Texture("light/light3.png");
    }
    
    public void update(float zAngle) {
        this.zAngle = zAngle;
    }

    public void render(Batch batch, float zAngle) {
        float lightSize = getRadius();
        Vector2 position = getPosition();
        batch.draw(light,
                position.x - lightSize * 0.5f, position.y - lightSize * 0.5f,
                lightSize, lightSize);
    }
    
    public void bind(int unit) {
        light.bind(unit);
    }
    
    public float getRadius() {
        return oscillate
                ? (magnitude * 0.75f + 0.25f * (float) Math.sin(zAngle) + .2f * MathUtils.random())
                : magnitude;
    }

    public abstract Vector2 getPosition();
    
    public static class OwnedLight extends Light {
        private final Locatable owner;
        
        public OwnedLight(Locatable owner) {
            super(5, false);
            this.owner = owner;
        }
        
        public OwnedLight(Locatable owner, LightDescription description) {
            super(description.getMagnitude(), description.getOscillate());
            this.owner = owner;
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
    
    public static class StaticLight extends Light {
        private final Vector2 position;
        
        public StaticLight(Vector2 position) {
            super(5, Math.random() < 0.2);
            this.position = position;
        }
        
        public StaticLight(Vector2 position, LightDescription description) {
            super(description.getMagnitude(), description.getOscillate());
            this.position = position;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }
    }
    
    public static class LightDescription {
        private final Rectangle bounds;
        private final float magnitude;
        private final boolean oscillate = false;
        
        public LightDescription(Rectangle bounds) {
            this.bounds = bounds;
            this.magnitude = 3;
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
    }
}
