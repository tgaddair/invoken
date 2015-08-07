package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.effects.AnimatedEffect;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Heuristics;
import com.google.common.base.Optional;

public class Burrow extends ActiveAugmentation {
    private static final TextureRegion[] DUST_REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/dust.png", 128, 128);
    private static final float DUST_SIZE = 4.5f;
    
    private static final float MINDST = 4;

    private static class Holder {
        private static final Burrow INSTANCE = new Burrow();
    }

    public static Burrow getInstance() {
        return Holder.INSTANCE;
    }

    private Burrow() {
        super(Optional.<String>absent());
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
        return !owner.isToggled(Burrow.class) ? new BurrowAction(owner) : new UnburrowAction(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.isToggled(Burrow.class)) {
            if (owner.dst2(target) > MINDST * MINDST || !owner.hasVisibilityTo(target)) {
                return 0f;
            }
            return Heuristics.randomizedDistanceScore(owner.dst2(target), 1);
        } else {
            if (!target.isAlive()) {
                return 0;
            }
            return owner == target || !target.hasVisibilityTo(owner) ? 2f : 0f;
        }
    }

    public class BurrowAction extends AnimatedAction {
        public BurrowAction(Agent owner) {
            super(owner, Activity.Swipe, Burrow.this);
        }

        @Override
        public void apply(Level level) {
            owner.toggleOn(Burrow.class);
            owner.addEffect(new Burrowed(owner));
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    public class UnburrowAction extends AnimatedAction {
        public UnburrowAction(Agent owner) {
            super(owner, Activity.Swipe, Burrow.this);
        }

        @Override
        public void apply(Level level) {
            owner.toggleOff(Burrow.class);
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    public static class Burrowed extends AnimatedEffect {
        private static final float MAX_DST2 = 5f * 5f;
        
        private final Player player;
        
        public Burrowed(Agent owner) {
            super(owner, DUST_REGIONS, Animation.PlayMode.LOOP, 2f, 2f);
            this.player = owner.getLocation().getPlayer();
        }
        
        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            float a = Math.max(1f - target.dst2(player) / MAX_DST2, 0f);
            renderer.getBatch().setColor(1, 1, 1, a);
            super.render(delta, renderer);
            renderer.getBatch().setColor(Color.WHITE);
        }

        @Override
        public boolean isFinished() {
            return !target.isAlive() || !target.isToggled(Burrow.class);
        }

        @Override
        public void dispel() {
            target.getBody().setActive(true);
            addDust(target);
        }

        @Override
        protected void doApply() {
            target.getBody().setActive(false);
            addDust(target);
        }
    }

    public static void addDust(Agent owner) {
        Level level = owner.getLocation();
        AnimatedEntity anim = new AnimatedEntity(DUST_REGIONS, owner.getPosition().cpy(),
                new Vector2(DUST_SIZE, DUST_SIZE), 0.05f);
        level.addEntity(anim);
    }
}
