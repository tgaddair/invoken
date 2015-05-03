package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.actor.type.Player;

public class HealthBar extends ProgressBar {
    private static final Color C = new Color(0xDC143CFF);
    private static final int W = Human.PX - 18;

    private final Batch batch;
    private Agent agent = null;

    public HealthBar(Skin skin) {
        super(0, 0, 1, false, skin);
        this.batch = new SpriteBatch();
        setWidth(W);
        // setColor(C);
    }

    public void update(Player player, Agent agent, Camera camera) {
        if (agent == null || player.canInteract(agent)) {
            this.agent = null;
            setVisible(false);
            return;
        }

        if (this.agent != agent) {
            this.agent = agent;
            setRange(0, agent.getInfo().getBaseHealth());
            setVisible(true);
        }

        if (agent.getInfo().getHealth() != getValue()) {
            setValue(agent.getInfo().getHealth());
        }
        setDisabled(!agent.isAlive());

        Vector2 position = agent.getRenderPosition();
        float h = agent.getHeight() / 2;
        Vector3 screen = camera.project(new Vector3(position.x, position.y + h, 0));
        setPosition(screen.x, screen.y - 10);
    }

    public void draw() {
        if (isVisible()) {
            batch.begin();
            draw(batch, 1.0f);
            batch.end();
        }
    }
}
