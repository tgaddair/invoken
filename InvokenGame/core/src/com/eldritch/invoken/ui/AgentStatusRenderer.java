package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.screens.GameScreen;

public class AgentStatusRenderer {
    private static Texture DIALOGUE_ICON = GameScreen.getTexture("icon/dialogue.png");
    private static float SCALE = 0.5f;

    public static void render(Agent agent, Player player, OrthogonalTiledMapRenderer renderer) {
        Vector2 position = agent.getRenderPosition();
        int i = 1;

        Batch batch = renderer.getSpriteBatch();
        if (agent.isAlive()) {
            if (player.canInteract(agent) && !agent.inDialogue() && agent.canSpeak()
                    && agent != player) {
                batch.draw(DIALOGUE_ICON, position.x, position.y + SCALE * i, SCALE, SCALE);
            }
        }
    }
}
