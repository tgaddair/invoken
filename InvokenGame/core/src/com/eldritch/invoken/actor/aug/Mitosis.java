package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

public class Mitosis extends ActiveAugmentation {
    private static class Holder {
        private static final Mitosis INSTANCE = new Mitosis();
    }

    public static Mitosis getInstance() {
        return Holder.INSTANCE;
    }

    private Mitosis() {
        super(Optional.<String>absent());
    }

    @Override
    public boolean isValid(Agent owner) {
        // can only perform once
        return !owner.isToggled(Mitosis.class);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new MitosisAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new MitosisAction(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.getInfo().getHealth() < owner.getInfo().getMaxHealth()) {
            return 5;
        }
        return 0;
    }

    public class MitosisAction extends AnimatedAction {
        public MitosisAction(Agent actor) {
            super(actor, Activity.Cast, Mitosis.this);
        }

        @Override
        public void apply(Level level) {
            owner.toggleOn(Mitosis.class);
            
            // every split reduces the max health of the twin
            Npc twin = level.createNpc(owner.getInfo().getId(), owner.getPosition());
            twin.getInfo().setMaxHealth(owner.getInfo().getHealth());
            twin.getInfo().resetHealth();
            
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.BUFF, owner.getPosition());
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
}
