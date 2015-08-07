package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;

/**
 * Containers use a TMX constraints file to define their position, but because they are animated,
 * the actual textures are loaded here.
 */
public class ContainerSmall extends LootContainer {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(GameScreen.ATLAS
            .findRegion("activators/container-small").split(32, 32));

    public ContainerSmall(NaturalVector2 position, Inventory inventory) {
        super(position, inventory, regions);
    }

    @Override
    protected void onOpen(Agent agent) {
        getInventory().releaseItems(agent.getLocation(), getCenter());
    }
    
    @Override
    protected void onEndInteraction(Agent interactor) {
        // do not close automatically
    }
    
    @Override
    protected boolean canActivate(Agent agent) {
        return super.canActivate(agent) && !isOpen();
    }
}
