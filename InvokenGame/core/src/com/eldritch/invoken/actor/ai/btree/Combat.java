package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.eldritch.invoken.actor.type.Npc;

public class Combat extends Sequence<Npc> {
    public Combat() {
        // first check that we have enemies, and failover to the next task if we don't
        addChild(new HasEnemies());

        // select the best combat strategy
        Selector<Npc> selector = new Selector<Npc>();

        // first we try to engage in combat with an enemy
        selector.addChild(new Attack());

        // if we cannot engage with an enemy, then we attempt to pursue our last seen enemy
        // TODO: flips into pursue, then back out as soon as it leaves cover, we should fix this
        // by perhaps letting pursue come first?
        Sequence<Npc> pursueSequence = new Sequence<Npc>();
        pursueSequence.addChild(new CanPursue());
        pursueSequence.addChild(new Invert<Npc>(new IsIntimidated()));
        pursueSequence.addChild(new HasSufficientEnergy());
        pursueSequence.addChild(new Pursue());
        selector.addChild(new PursueOrExplore(pursueSequence));

        // whatever strategy we pick, we will not failover to the next task at this point
        addChild(new AlwaysSucceed<Npc>(selector));
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }

    private static class HasEnemies extends BooleanTask {
        @Override
        protected boolean check(Npc entity) {
            return entity.isCombatReady();
        }
    }
    
    private static class CanPursue extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            if (npc.getInventory().hasRangedWeapon()) {
                return npc.getPosition().dst2(npc.getLastSeen().getPosition()) > 5;
            } else {
                return !npc.getLastSeen().hasArrived();
            }
        }
    }
    
    private static class HasSufficientEnergy extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.getInfo().getEnergyPercent() > 0.25f;
        }
    }
}
