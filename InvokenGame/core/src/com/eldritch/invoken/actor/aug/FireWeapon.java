package com.eldritch.invoken.actor.aug;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Action;
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Direction;
import com.eldritch.invoken.screens.GameScreen;

public class FireWeapon extends Augmentation {
	@Override
	public Action getAction(Agent owner, Agent target) {
		return new FireAction(owner, target);
	}
	
	public class FireAction implements Action {
		private final float width;
		private final float height;
		private final Map<Direction, Animation> animations = new HashMap<Direction, Animation>();
		
		private final Agent owner;
		private final Agent target;
		private float stateTime = 0;
		
		public FireAction(Agent actor, Agent target) {
			this.owner = actor;
			this.target = target;
			
			TextureRegion[][] regions = GameScreen.getRegions(
					"sprite/effects/muzzle-flash.png", 48, 48);
			for (Direction d : Direction.values()) {
				Animation anim = new Animation(0.05f, regions[d.ordinal()]);
				anim.setPlayMode(Animation.PlayMode.NORMAL);
				animations.put(d, anim);
			}
			
			width = 1 / 32f * regions[0][0].getRegionWidth();
			height = 1 / 32f * regions[0][0].getRegionHeight();
		}
		
		@Override
		public void render(float delta, OrthogonalTiledMapRenderer renderer) {
			stateTime += delta;
			TextureRegion frame = getAnimation().getKeyFrame(stateTime);
			Vector2 position = owner.getPosition();
			
			Batch batch = renderer.getSpriteBatch();
			batch.begin();
			batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
			batch.end();
		}

		@Override
		public boolean isFinished() {
			return getAnimation().isAnimationFinished(stateTime);
		}

		@Override
		public void apply() {
			if (target != null) {
				owner.getEnemies().add(target);
				target.damage(owner, 1);
			}
		}
		
		private Animation getAnimation() {
			return animations.get(owner.getDirection());
		}
	}
}
