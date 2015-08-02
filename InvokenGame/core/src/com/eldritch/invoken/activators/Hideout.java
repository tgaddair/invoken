package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.SelectionHandler;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public class Hideout extends ClickActivator {
    private static final int W = 1;
    private static final int H = 2;

    public Hideout(NaturalVector2 position) {
        super(position.x, position.y, W, H, ProximityParams.of(
                new Vector2(position.x + W / 2f, position.y + H / 2f)).withIndicator(
                getIndicator(new Vector2(W / 2f, H))));
    }
    
    @Override
    public void activate(Agent agent, Level level) {
        hide(agent);
    }

    private void hide(Agent agent) {
        Vector2 center = getCenter();
        agent.setActive(false);
        agent.teleport(new Vector2(center.x, center.y));
        agent.setSelectionHandler(new HideoutSelectionHandler(agent));
    }
    
    private void reveal(Agent agent) {
        Vector2 center = getCenter();
        agent.removeSelectionHandler();
        agent.teleport(new Vector2(center.x, center.y - H + 0.5f));
        agent.setActive(true);
    }

    @Override
    protected void postRegister(Level level) {
    }
    
    private class HideoutSelectionHandler implements SelectionHandler {
        private final Agent owner;
        
        public HideoutSelectionHandler(Agent owner) {
            this.owner = owner;
        }
        
        @Override
        public boolean canSelect(Agent other) {
            return other == null;
        }

        @Override
        public boolean select(Agent other) {
            reveal(owner);
            return true;
        }
    }
}
