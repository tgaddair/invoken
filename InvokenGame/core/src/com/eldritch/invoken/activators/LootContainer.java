package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Lootable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Settings;

/**
 * Containers use a TMX constraints file to define their position, but because they are animated,
 * the actual textures are loaded here.
 */
public abstract class LootContainer extends InteractableActivator implements Lootable {
    private final Inventory inventory;
    private final Animation animation;
    private boolean activating = false;
    private float stateTime = 0;
    private boolean open = false;

    public LootContainer(NaturalVector2 position, Inventory inventory, TextureRegion[] regions) {
        super(position);
        this.inventory = inventory;
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void postUpdate(float delta, Level level) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation
                        .setPlayMode(mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
                open = !open;

                Agent agent = getInteractor();
                if (open && agent != null) {
                    onOpen(agent);
                }
            }
        }
    }

    @Override
    public void preRender(float delta, OrthogonalTiledMapRenderer renderer) {
        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getRenderPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected void onBeginInteraction(Agent interactor) {
        activating = true;
    }

    @Override
    protected void onEndInteraction(Agent interactor) {
        // close
        activating = true;
    }
    
    protected abstract void onOpen(Agent agent);
}
