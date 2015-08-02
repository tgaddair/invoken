package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Teleported;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Heuristics;

public class Teleport extends ActiveAugmentation {
    private static final float IDEAL_DST = 5f;
    
    private final Vector2 tmp = new Vector2();
    
    private static class Holder {
        private static final Teleport INSTANCE = new Teleport();
    }

    public static Teleport getInstance() {
        return Holder.INSTANCE;
    }

    private Teleport() {
        super("teleport");
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner, getDestination(target));
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        NaturalVector2 point = NaturalVector2.of(target);
        return owner.getLocation().getMap().isClearGround(point.x, point.y);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return getAction(owner, getDestination(target));
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new TeleportAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.hasVisibilityTo(owner) && target.inFieldOfView(owner)) {
            float dst2 = owner.dst2(target);
            if (dst2 > IDEAL_DST * IDEAL_DST) {
                // too far to merit recall
                return 0;
            }
            
            float score = Heuristics.randomizedDistanceScore(dst2, IDEAL_DST * IDEAL_DST);
            return score;
        }
        
        return 0;
    }
    
    private Vector2 getDestination(Agent target) {
        return tmp.set(target.getPosition()).add(target.getBackwardVector().scl(IDEAL_DST));
    }

    public class TeleportAction extends AnimatedAction {
        private final NaturalVector2 target;
        
        public TeleportAction(Agent owner, Vector2 target) {
            super(owner, Activity.Swipe, Teleport.this);
            this.target = NaturalVector2.of(target);
        }

        @Override
        public void apply(Level level) {
            owner.addEffect(new Teleported(owner, target));
            owner.resetMark();
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
}
