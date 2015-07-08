package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Jaunt;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.DodgeHandler.AbstractDodgeHandler;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Jaunting extends BasicEffect {
    private static final float RADIUS = 2.5f;

    private final Vector2 direction = new Vector2();

    public Jaunting(Agent agent, Vector2 target) {
        super(agent);
    }

    @Override
    public boolean isFinished() {
        return !target.isToggled(Jaunt.class);
    }

    @Override
    public void dispel() {
        target.toggleOff(Jaunt.class);
    }

    @Override
    protected void doApply() {
        getTarget().setDodgeHandler(new JauntHandler(getTarget()));
    }

    @Override
    protected void update(float delta) {
    }

    public class JauntHandler extends AbstractDodgeHandler {
        public static final float DODGE_SCALE = 750f;
        public static final float DODGE_COST = 20f;

        public JauntHandler(Agent agent) {
            super(agent, DODGE_SCALE, DODGE_COST);
        }

        @Override
        public boolean isFinished() {
            return Jaunting.this.isFinished();
        }

        @Override
        public void onDodgeComplete() {
            Agent owner = getTarget();
            owner.stop();
            
            Level level = owner.getLocation();
            Vector2 center = owner.getPosition();

            level.addEntity(AnimatedEntity.createSmokeRing(center, RADIUS * 2));
            for (Agent neighbor : owner.getNeighbors()) {
                if (neighbor.inRange(center, RADIUS)) {
                    float scale = Heuristics.distanceScore(center.dst2(neighbor.getPosition()), 0);
                    direction.set(neighbor.getPosition()).sub(owner.getPosition()).nor().scl(scale);
                    neighbor.applyForce(direction.scl(500));
                    neighbor.addEffect(new Stunned(owner, neighbor, 1f));
                    level.addEntity(AnimatedEntity.createDisintegrate(neighbor.getPosition()));

                    InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_HIT,
                            neighbor.getPosition());
                }
            }
        }
    }
}
