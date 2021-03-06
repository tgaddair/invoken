package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.SelectionHandler;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.google.common.base.Optional;

public class Hideout extends ClickActivator {
    private static final int W = 1;
    private static final int H = 2;
    
    private Optional<HideoutSelectionHandler> selectionHandler = Optional.absent();

    public Hideout(NaturalVector2 position) {
        super(position.x, position.y, W, H, ProximityParams.of(
                new Vector2(position.x + W / 2f, position.y + H / 2f)).withIndicator(
                getIndicator(new Vector2(W / 2f, H))));
    }
    
    @Override
    public void postUpdate(float delta, Level level) {
        if (selectionHandler.isPresent()) {
            HideoutSelectionHandler handler = selectionHandler.get();
            if (handler.isCancelled()) {
                reveal(handler.getOwner(), handler.getPosition());
            }
        }
    }
    
    @Override
    public void activate(Agent agent, Level level) {
        hide(agent);
    }

    private void hide(Agent agent) {
        Vector2 center = getCenter();
        HideoutSelectionHandler handler = new HideoutSelectionHandler(agent);
        
        agent.setActive(false);
        agent.teleport(new Vector2(center.x, center.y));
        
        agent.setSelectionHandler(handler);
        selectionHandler = Optional.of(handler);
    }
    
    private void reveal(Agent agent, Vector2 position) {
        agent.removeSelectionHandler();
        agent.teleport(position);
        agent.setActive(true);
        selectionHandler = Optional.absent();
    }

    @Override
    protected void postRegister(Level level) {
    }
    
    @Override
    protected boolean canActivate(Agent agent) {
        return super.canActivate(agent) && getProximityAgentsCount() == 1 && hasProximity(agent);
    }

    @Override
    protected void onProximityAdd(Agent agent) {
        if (selectionHandler.isPresent()) {
            selectionHandler.get().cancel();
        }
    }
    
    private class HideoutSelectionHandler implements SelectionHandler {
        private final Agent owner;
        private final Vector2 position = new Vector2();
        private boolean cancelled = false;
        
        public HideoutSelectionHandler(Agent owner) {
            this.owner = owner;
            this.position.set(owner.getPosition());
        }
        
        @Override
        public boolean canSelect(Agent other) {
            return other == null;
        }

        @Override
        public boolean select(Agent other) {
            cancel();
            return true;
        }
        
        private void cancel() {
            cancelled = true;
        }
        
        private boolean isCancelled() {
            return cancelled;
        }
        
        private Agent getOwner() {
            return owner;
        }
        
        private Vector2 getPosition() {
            return position;
        }
    }
}
