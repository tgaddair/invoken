package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.google.common.base.Optional;

public class Stasis extends ActiveAugmentation {
    private static final TextureRegion REGION = new TextureRegion(
            GameScreen.getTexture("sprite/effects/stasis.png"));

    private static class Holder {
        private static final Stasis INSTANCE = new Stasis();
    }

    public static Stasis getInstance() {
        return Holder.INSTANCE;
    }

    private Stasis() {
        super(Optional.<String> absent());
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return getAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return getAction(owner);
    }

    private Action getAction(Agent owner) {
        return new StasisAction(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.isToggled(Stasis.class)) {
            return 0f;
        }
        return !owner.getLocation().inLockdown() ? 5f : 0f;
    }

    public class StasisAction extends AnimatedAction {
        public StasisAction(Agent owner) {
            super(owner, Activity.Swipe, Stasis.this);
        }

        @Override
        public void apply(Level level) {
            owner.toggleOn(Stasis.class);
            owner.addEffect(new InStasis(owner));
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    public static class InStasis extends BasicEffect {
        private final float width;
        private final float height;
        private boolean finished = false;
        
        public InStasis(Agent owner) {
            super(owner);
            this.width = owner.getWidth();
            this.height = owner.getHeight();
        }

        @Override
        public boolean isFinished() {
            return finished || !target.isAlive() || !target.isToggled(Stasis.class);
        }

        @Override
        protected void doApply() {
            target.setEnabled(false);
        }

        @Override
        public void dispel() {
            target.toggleOff(Stasis.class);
            target.setEnabled(true);
        }

        @Override
        protected void update(float delta) {
            if (target.getLocation().inLockdown()) {
                finished = true;
            }
        }
        
        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            Vector2 position = target.getRenderPosition();
            
            Batch batch = renderer.getBatch();
            batch.begin();
            batch.draw(REGION, position.x - width / 2, position.y - height / 2, width, height);
            batch.end();
        }
    }
}
