package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Human;

public class HealthBar extends ProgressBar {
    private static final Color C = new Color(1, 0.3f, 0.3f, 1);
    private static final int W = Human.PX - 10;
    
    private Agent agent = null;
    
    public HealthBar(Skin skin) {
        super(0, 0, 1, false, skin);
        setWidth(W);
        setColor(C);
    }

    public void update(Agent agent, Camera camera) {
        if (agent == null) {
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
        
        Vector2 position = agent.getPosition();
        float h = agent.getHeight() / 2;
        Vector3 screen = camera.project(new Vector3(position.x, position.y + h, 0));
        setPosition(screen.x, screen.y - 10);
    }
}
