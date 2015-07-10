package com.eldritch.invoken.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.screens.GameScreen;

public class AgentStatusRenderer {
    private static Texture DIALOGUE_ICON = GameScreen.getTexture("icon/dialogue.png");
    private static Texture LOOT_ICON = GameScreen.getTexture("icon/loot.png");
    private static float SCALE = 0.5f;

    public static void render(Agent agent, Player player, OrthogonalTiledMapRenderer renderer) {
        if (agent == player) {
            // cannot interact without ourselves
            return;
        }
        
        Vector2 position = agent.getRenderPosition();
        int i = 1;

        Batch batch = renderer.getBatch();
        if (agent.isAlive()) {
            if (player.canInteract(agent) && !agent.inDialogue() && agent.canSpeak()) {
                draw(batch, DIALOGUE_ICON, position, i);
            }
        } else {
            if (player.canInteract(agent)) {
                boolean empty = agent.getInventory().isEmpty();
                if (empty) {
                    batch.setColor(Color.GRAY);
                    draw(batch, LOOT_ICON, position, i);
                    batch.setColor(Color.WHITE);
                } else {
                    draw(batch, LOOT_ICON, position, i);
                }
            }
        }
    }
    
    private static void draw(Batch batch, Texture icon, Vector2 position, int i) {
        batch.draw(icon, position.x, position.y + SCALE * i, SCALE, SCALE);
    }
}
