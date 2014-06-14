package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;

public abstract class Light {
    private final float magnitude;
    private boolean oscillate;
    private Texture light;

    public Light(float magnitude, boolean oscillate) {
        this.magnitude = magnitude;
        this.oscillate = oscillate;
        light = new Texture("light/light2.png");
    }

    public void render(Batch batch, float zAngle) {
        float lightSize = oscillate
                ? (magnitude * 0.75f + 0.25f * (float) Math.sin(zAngle) + .2f * MathUtils.random())
                : magnitude;
        Vector2 position = getPosition();
        batch.draw(light,
                position.x - lightSize * 0.5f, position.y - lightSize * 0.5f,
                lightSize, lightSize);
    }
    
    public void bind(int unit) {
        light.bind(unit);
    }
    
    public abstract Vector2 getPosition();
    
    public static class AgentLight extends Light {
        private final Agent owner;
        
        public AgentLight(Agent owner) {
            super(15, false);
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
            super(10, Math.random() < 0.2);
            this.position = position;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }
    }
}
