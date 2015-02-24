package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;

/**
 * Containers use a TMX constraints file to define their position, but because they are animated,
 * the actual textures are loaded here.
 */
public class ContainerSmall extends ClickActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(GameScreen.ATLAS
            .findRegion("activators/container-small").split(32, 32));

    private final Animation animation;
    private boolean activating = false;
    private float stateTime = 0;

    public ContainerSmall(NaturalVector2 position) {
        super(position);
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void activate(Agent agent, Location location) {
        activating = true;
    }

    @Override
    public void register(Location location) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation
                        .setPlayMode(mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
            }
        }

        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }
}
