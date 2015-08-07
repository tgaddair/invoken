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
public class ContainerLarge extends LootContainer {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(GameScreen.ATLAS
            .findRegion("activators/container-large").split(32, 32));

    public ContainerLarge(NaturalVector2 position, Inventory inventory) {
        super(position, inventory, regions);
    }

    @Override
    protected void onOpen(Agent agent) {
        // begin looting
        agent.beginLooting(this);
    }
}
