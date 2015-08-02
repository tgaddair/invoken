package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;

public class Teleported extends BasicEffect {
//    private static final TextureRegion[] FLASH_REGIONS = GameScreen.getMergedRegion(
//            "sprite/effects/flash.png", 128, 128);
//    private static final float FLASH_SIZE = 2.5f;

    private final NaturalVector2 destination;
    private boolean applied = false;

    public Teleported(Agent owner, NaturalVector2 destination) {
        super(owner);
        this.destination = destination;
    }

    @Override
    public boolean isFinished() {
        return applied;
    }

    @Override
    public void dispel() {
    }

    @Override
    protected void doApply() {
//        Level level = target.getLocation();
//        AnimatedEntity sourceAnim = new AnimatedEntity(FLASH_REGIONS, target.getPosition().cpy(),
//                new Vector2(FLASH_SIZE, FLASH_SIZE), 0.025f);
//        level.addEntity(sourceAnim);
        
        Vector2 destination2 = destination.getCenter();
        target.teleport(destination2);
        applied = true;
        
        // we need to update the distance cache for things like rendering
        for (Agent neighbor : target.getNeighbors()) {
            target.setDst2(neighbor);
        }

//        AnimatedEntity destAnim = new AnimatedEntity(FLASH_REGIONS, destination2, new Vector2(
//                FLASH_SIZE, FLASH_SIZE), 0.025f);
//        level.addEntity(destAnim);
    }

    @Override
    protected void update(float delta) {
    }
}
