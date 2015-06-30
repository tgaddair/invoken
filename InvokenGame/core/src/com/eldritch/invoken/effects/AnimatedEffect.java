package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;

public abstract class AnimatedEffect extends BasicEffect {
	private final float width;
	private final float height;
	private final Vector2 offset;
	private final float theta;
	private Animation animation;
	
	public AnimatedEffect(Agent actor, TextureRegion[] region) {
		this(actor, region, Vector2.Zero);
	}
	
	public AnimatedEffect(Agent actor, TextureRegion[] region, Vector2 offset) {
		this(actor, region, offset, 0, Animation.PlayMode.NORMAL, 0.1f);
	}
	
	public AnimatedEffect(Agent actor, TextureRegion[] region, Animation.PlayMode playMode) {
		this(actor, region, Vector2.Zero, 0, playMode, 0.1f);
	}
	
	public AnimatedEffect(Agent actor, TextureRegion[] region, Vector2 offset, float theta,
			Animation.PlayMode playMode, float frameDuration) {
	    super(actor);
	    this.offset = offset;
	    this.theta = theta;
		
		animation = new Animation(frameDuration, region);
		animation.setPlayMode(playMode);
		
		width = 1 / 32f * region[0].getRegionWidth();
		height = 1 / 32f * region[0].getRegionHeight();
	}
	
	@Override
    public void update(float delta) {
    }
	
	@Override
	public void render(float delta, OrthogonalTiledMapRenderer renderer) {
		TextureRegion frame = animation.getKeyFrame(getStateTime());
		Vector2 position = target.getRenderPosition();
		
		Batch batch = renderer.getBatch();
		batch.begin();
		batch.draw(frame, 
				position.x - width / 2 - offset.x, position.y - height / 2 - offset.y, // position
                width / 2, height / 2, // origin
                width, height, // size
                1f, 1f, // scale
                theta);
		batch.end();
	}
	
	@Override
	public boolean isFinished() {
		return animation.isAnimationFinished(getStateTime());
	}
}
