package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;

public class DoorActivator extends ClickActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(
            "sprite/activators/blast-door.png", 64, 96);
    
    // for bounding area
    private static final int WIDTH = 2;
    
    private final Animation animation;
    private boolean open = false;
    private boolean locked = false;
    
    private Body body;
    
    private boolean activating = false;
    private float stateTime = 0;
    
    public static DoorActivator createFront(int x, int y) {
        Animation animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        return new DoorActivator(x, y, animation);
    }

    public DoorActivator(int x, int y, Animation animation) {
    	super(NaturalVector2.of(x, y), 2, 2);
        this.animation = animation;
//        locked = Math.random() < 0.5;
    }

    @Override
    public void activate(Agent agent, Location location) {
        if (locked) {
            // unlock
            locked = false;
            location.alertTo(agent);
            return;
        }
        
        activating = true;
        open = !open;
        body.setActive(!open);
    }

	@Override
	public void register(Location location) {
	    Vector2 position = getPosition();
	    int x = (int) position.x;
	    int y = (int) position.y;
	    body = location.createEdge(x, y, x + WIDTH, y);
	}

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation.setPlayMode(
                        mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
            }
        }
        
        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y,
                frame.getRegionWidth() * Settings.SCALE, frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }
    
    @Override
    public float getZ() {
        if (open && !activating) {
            // always draw below everything else when open
            return Float.POSITIVE_INFINITY;
        }
        return getPosition().y;
    }
}
