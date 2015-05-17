package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.actor.type.Agent;

public class StatusBar<T extends Agent> extends ProgressBar {
//    private static final Color HEALTH_COLOR = new Color(0xFF1414FF);
    
    private final T agent;
    private final StatusCalculator<T> calculator;
    
    public StatusBar(T agent, StatusCalculator<T> calculator, Skin skin) {
        super(0, calculator.getBaseStatus(agent), 1, true, skin, calculator.getStyleName());
        this.agent = agent;
        this.calculator = calculator;
    }

    public void update() {
        float value = calculator.getStatus(agent);
        if (value != getValue()) {
            setValue(value);
        }
    }
    
    public void resize(int width, int height) {
    }
    
    public static class HealthCalculator implements StatusCalculator<Agent> {
        @Override
        public float getStatus(Agent agent) {
            return agent.getInfo().getHealth();
        }

        @Override
        public float getBaseStatus(Agent agent) {
            return agent.getInfo().getBaseHealth();
        }
        
        @Override
        public String getStyleName() {
            return "health-vertical";
        }
    }
    
    public static class EnergyCalculator implements StatusCalculator<Agent> {
        @Override
        public float getStatus(Agent agent) {
            return agent.getInfo().getEnergy();
        }

        @Override
        public float getBaseStatus(Agent agent) {
            return agent.getInfo().getMaxEnergy();
        }

        @Override
        public String getStyleName() {
            return "default-vertical";
        }
    }
    
    public interface StatusCalculator<T extends Agent> {
        float getStatus(T agent);
        
        float getBaseStatus(T agent);
        
        String getStyleName();
    }
}
