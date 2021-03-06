package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.effects.Paralyzed;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class Paralyze extends ProjectileAugmentation {
	private static class Holder {
        private static final Paralyze INSTANCE = new Paralyze();
	}
	
	public static Paralyze getInstance() {
		return Holder.INSTANCE;
	}
	
    private Paralyze() {
        super("paralyze");
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ParalyzeAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return 3;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return !target.isParalyzed() ? super.quality(owner, target, level) * 5 : 0;
    }

    public class ParalyzeAction extends AnimatedAction {
        private final Vector2 target;

        public ParalyzeAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Paralyze.this);
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            ParalyzeBullet bullet = new ParalyzeBullet(owner);
            level.addEntity(bullet);
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public static class ParalyzeBullet extends HandledBullet {
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private final Animation animation;

        public ParalyzeBullet(Agent owner) {
            super(owner, regions[0], fixedSentryDirection(owner), 10, Damage.from(owner));
            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        @Override
        protected void preRender(Batch batch) {
            batch.setColor(Color.GREEN);
        }

        @Override
        protected void postRender(Batch batch) {
            batch.setColor(Color.WHITE);
        }

        @Override
        protected void apply(Agent owner, Agent target, Vector2 contact) {
            target.addEffect(new Paralyzed(owner, target, 3));
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return animation.getKeyFrame(stateTime);
        }
    }
}
