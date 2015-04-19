package com.eldritch.invoken.activators;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.Lootable;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.Settings;

/**
 * Containers use a TMX constraints file to define their position, but because they are animated,
 * the actual textures are loaded here.
 */
public class ContainerSmall extends ClickActivator implements Lootable {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(GameScreen.ATLAS
            .findRegion("activators/container-small").split(32, 32));

    private final Inventory inventory;
    private final Animation animation;
    private boolean activating = false;
    private float stateTime = 0;
    private Agent agent = null;
    private boolean open = false;
    
    public ContainerSmall(NaturalVector2 position) {
        this(position, new Inventory(new ArrayList<InventoryItem>()));
    }

    public ContainerSmall(NaturalVector2 position, Inventory inventory) {
        super(position);
        this.inventory = inventory;
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void activate(Agent agent, Location location) {
        activating = true;
        this.agent = agent;
    }

    @Override
    public void register(Location location) {
    }
    
    @Override
    public void update(float delta, Location location) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation
                        .setPlayMode(mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
                open = !open;
                if (open && agent != null) {
                    // begin looting
                    agent.beginLooting(this);
                }
            }
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

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
    public boolean canInteract() {
        return true;
    }

    @Override
    public void endInteraction() {
    }
}
