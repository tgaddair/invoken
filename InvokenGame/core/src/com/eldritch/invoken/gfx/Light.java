package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Agent;

public class Light {
    private final Agent owner;
    
    private Texture light;
    private boolean lightOscillate;

    public Light(Agent owner) {
        this.owner = owner;
        light = new Texture("light/light.png");
    }

    public void render(Batch batch, float zAngle) {
        float lightSize = lightOscillate
                ? (4.75f + 0.25f * (float) Math.sin(zAngle) + .2f * MathUtils.random())
                : 25.0f;
        Vector2 position = owner.getPosition();
        batch.draw(light,
                position.x - lightSize * 0.5f, position.y - lightSize * 0.5f,
                lightSize, lightSize);
    }
    
    public void bind(int unit) {
        light.bind(unit);
    }
}
