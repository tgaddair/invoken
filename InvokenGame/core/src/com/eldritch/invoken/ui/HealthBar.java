package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.util.Damageable;

public class HealthBar extends ProgressBar {
    private static final Color C = new Color(0xDC143CFF);
    private static final float W = Human.PX - 18;

    private final Batch batch;
    private final Vector3 worldPosition = new Vector3();
    private Damageable entity = null;

    public HealthBar(Skin skin) {
        super(0, 0, 1, false, skin);
        this.batch = new SpriteBatch();
        setWidth(W);
        // setColor(C);
    }

    public void update(Player player, Agent agent, Camera camera) {
        if (agent == null || player.canInteract(agent)) {
            this.entity = null;
            setVisible(false);
            return;
        }

        update(agent);
    }
    
    public void update(Damageable entity) {
        if (this.entity != entity) {
            this.entity = entity;
            setRange(0, entity.getBaseHealth());
            setVisible(true);
        }

        if (entity.getHealth() != getValue()) {
            setValue(entity.getHealth());
        }
        setDisabled(!entity.isAlive());
    }

    public void draw(Camera camera) {
        if (isVisible()) {
            entity.setHealthIndicator(worldPosition);
            Vector3 screen = camera.project(worldPosition);
            setPosition(screen.x, screen.y - 10);
            
            batch.begin();
            draw(batch, 1.0f);
            batch.end();
        }
    }
}
