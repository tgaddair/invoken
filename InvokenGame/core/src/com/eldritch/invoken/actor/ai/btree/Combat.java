package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
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
        Sequence<Npc> pursueSequence = new Sequence<Npc>();
        pursueSequence.addChild(new CanPursue());
        pursueSequence.addChild(new HasSufficientEnergy());
        pursueSequence.addChild(new Pursue());
        selector.addChild(pursueSequence);
        
        // when we can no longer pursue our target, fall back to wandering
        selector.addChild(new Idle());

        // whatever strategy we pick, we will not failover to the next task at this point
        addChild(new AlwaysSucceed<Npc>(selector));
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }

    private static class HasEnemies extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (entity.isCombatReady()) {
                success();
            } else {
                fail();
            }
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
    
    private static class CanPursue extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (check(entity)) {
                success();
            } else {
                fail();
            }
        }
        
        private boolean check(Npc npc) {
            return npc.getPosition().dst2(npc.getLastSeen().getPosition()) > 1;
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
    
    private static class HasSufficientEnergy extends LeafTask<Npc> {
        @Override
        public void run(Npc entity) {
            if (check(entity)) {
                success();
            } else {
                fail();
            }
        }
        
        private boolean check(Npc npc) {
            return npc.getInfo().getEnergyPercent() > 0.25f;
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
}
