package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Resurrected;
import com.eldritch.invoken.effects.SummonEnergy;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;

public class Resurrect extends Augmentation {
    private static final TextureRegion[] SPLASH_REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/envelop.png", 128, 128);
    private static final float SPLASH_SIZE = 2f;

    private static final int BASE_COST = 30;

    private static class Holder {
        private static final Resurrect INSTANCE = new Resurrect();
    }

    public static Resurrect getInstance() {
        return Holder.INSTANCE;
    }

    private Resurrect() {
        super("resurrect");
    }

    @Override
    public Action getBestAction(Agent owner, Agent target) {
        // TODO: look nearby
        return getAction(owner, target);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new ResurrectAction(owner, target);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return getAction(owner, owner.getTarget());
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && !target.isAlive()
                && owner.hasLineOfSight(target);
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner, owner.getTarget());
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return 5;
    }

    @Override
    protected void setBestTarget(Agent agent, Agent goal, Target target) {
        for (Agent neighbor : goal.getNeighbors()) {
            if (!neighbor.isAlive()) {
                target.set(neighbor);
            }
        }
    }

    public class ResurrectAction extends AnimatedAction {
        private final Agent target;

        public ResurrectAction(Agent owner, Agent target) {
            super(owner, Activity.Cast, Resurrect.this, new SummonEnergy(owner));
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            if (!target.isAlive()) {
                level.addEntity(new AnimatedEntity(SPLASH_REGIONS, target.getPosition(),
                        new Vector2(SPLASH_SIZE, SPLASH_SIZE), 0.1f));
                target.addEffect(new Resurrected(owner, target, BASE_COST));
            }
        }

        @Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
    }
}
