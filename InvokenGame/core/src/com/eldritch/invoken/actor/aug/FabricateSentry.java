package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Fabricated;
import com.eldritch.invoken.effects.SummonEnergy;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Callback;

public class FabricateSentry extends ActiveAugmentation {
    private static final TextureRegion[] SPLASH_REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/envelop.png", 128, 128);
    private static final String ID = "SmallSentrySummoned";
    private static final float SPLASH_SIZE = 2f;

    private static final int BASE_COST = 20;

    private final Vector2 tmp = new Vector2();

    private static class Holder {
        private static final FabricateSentry INSTANCE = new FabricateSentry();
    }

    public static FabricateSentry getInstance() {
        return Holder.INSTANCE;
    }

    private FabricateSentry() {
        super("fabricate-sentry");
    }

    @Override
    public Action getBestAction(Agent owner, Agent target) {
        // TODO: look nearby
        return getAction(owner, target);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        if (canDestroy(owner, target)) {
            return new DestroyAction(owner, target);
        }

        return getAction(owner, target.getPosition());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new FabricateAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        if (canDestroy(owner, target)) {
            return true;
        }
        return isValid(owner, target.getPosition());
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        NaturalVector2 point = NaturalVector2.of(target);
        return owner.getLocation().getMap().isClearGround(point.x, point.y);
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!owner.hasVisibilityTo(target)) {
            return 0;
        }
        return 5;
    }

    private boolean canDestroy(Agent owner, Agent target) {
        return target.getInfo().getId().equals(ID) && target.isFollowing(owner)
                && !target.isConfused();
    }

    @Override
    protected void setBestTarget(Agent agent, Agent goal, Target target) {
        // find the midpoint between this agent and the goal
        Vector2 v1 = agent.getPosition();
        Vector2 v2 = goal.getPosition();
        Vector2 midpoint = tmp.set((v1.x + v2.x) / 2f, (v1.y + v2.y) / 2f);
        if (isValid(agent, midpoint)) {
            target.set(midpoint);
        }
    }

    public class FabricateAction extends AnimatedAction {
        private final Vector2 target;

        public FabricateAction(Agent owner, Vector2 target) {
            super(owner, Activity.Cast, FabricateSentry.this, new SummonEnergy(owner));
            this.target = NaturalVector2.of(target).getCenter();
        }

        @Override
        public void apply(Level level) {
            AnimatedEntity entity = new AnimatedEntity(SPLASH_REGIONS, target, new Vector2(
                    SPLASH_SIZE, SPLASH_SIZE), 0.1f);
            entity.withCallback(new Callback() {
                @Override
                public void call() {
                    owner.addEffect(new Fabricated(owner, ID, target, BASE_COST));
                }
            });
            level.addEntity(entity);
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public class DestroyAction extends AnimatedAction {
        private final Agent target;

        public DestroyAction(Agent owner, Agent target) {
            super(owner, Activity.Cast, FabricateSentry.this, new SummonEnergy(owner));
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            target.kill();
        }

        @Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
        
        @Override
        public int getCost() {
            return 0;
        }
    }
}
