package com.eldritch.invoken.actor.ai;

import java.util.List;

import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.btree.Pursue;
import com.eldritch.invoken.actor.aug.Ping;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public abstract class Planner {
    private static final float MIN_DST2 = 9f;
    
    protected final Npc owner;
    
    public Planner(Npc owner) {
        this.owner = owner;
    }
    
    public abstract void plan(float delta);
    
    public abstract boolean act();
    
    public abstract boolean hasGoal();
    
    public static class DefaultPlanner extends Planner {
        public DefaultPlanner(Npc owner) {
            super(owner);
        }

        @Override
        public void plan(float delta) {
        }

        @Override
        public boolean act() {
            return false;
        }

        @Override
        public boolean hasGoal() {
            return false;
        }
    }
    
    public static class HunterPlanner extends Planner {
        private static final float DURATION = 5f;
        
        private float elapsed = 0;
        
        public HunterPlanner(Npc owner) {
            super(owner);
        }

        @Override
        public void plan(float delta) {
            elapsed += delta;
        }

        @Override
        public boolean act() {
            if (getPrey() == null || elapsed > DURATION) {
                if (!ping()) {
                    // failed to ping, so we cannot proceed
                    return false;
                }
                elapsed = 0;
            }
            
            Agent target = owner.getLastSeen().getTarget();
            if (target != null && owner.isEnemy(target)) {
                Pursue.act(owner);
                return true;
            }
            return false;
        }
        
        private Agent getPrey() {
            return owner.getLastSeen().getTarget();
        }
        
        private boolean ping() {
            PreparedAugmentations augs = owner.getInfo().getAugmentations();
            if (augs.isPrepared(Ping.getInstance())) {
                if (augs.use(Ping.getInstance())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasGoal() {
            return true;
        }
    }
    
    public static class GuardPlanner extends Planner {
        private static final float DURATION = 20f;
        
        private Agent destination = null;
        private float elapsed = 0;
        
        public GuardPlanner(Npc owner) {
            super(owner);
        }
        
        @Override
        public void plan(float delta) {
            if (owner.isGuard() && isLeader()) {
                if (isLeader()) {
                    planForLeader(delta);
                } else if (owner.hasSquad()) {
                    planForGuard(delta);
                }
            }
        }
        
        private void planForGuard(float delta) {
            if (destination == null) {
                setDestination(owner.getSquad().getLeader());
            }
        }
        
        private void planForLeader(float delta) {
            if (!hasGoal()) {
                setDestination(delta);
            } else {
                if (destination != null) {
                    updateDestination(delta);
                }
            }
        }
        
        private void updateDestination(float delta) {
            elapsed += delta;
            if (destination == owner || owner.dst2(destination) < MIN_DST2 || elapsed > DURATION) {
                setDestination(delta);
            }
        }
        
        private void setDestination(float delta) {
            List<Agent> agents = owner.getLocation().getAllAgents();
            Agent agent = agents.get((int) (Math.random() * agents.size()));
            
            // navigate towards this agent as part of a routine patrol
            setDestination(agent);
        }
        
        private void setDestination(Agent destination) {
            this.destination = destination;
            elapsed = 0;
        }
        
        private boolean isLeader() {
            if (!owner.hasSquad()) {
                return true;
            }
            return owner.getSquad().getLeader() == owner;
        }

        @Override
        public boolean act() {
            if (destination != null) {
                owner.setTarget(destination);
                Pursue.act(owner);
                return true;
            }
            return false;
        }

        @Override
        public boolean hasGoal() {
            return destination != null;
        }
    }
    
    public static Planner from(Npc npc) {
        if (npc.isGuard()) {
            return new GuardPlanner(npc);
        }
        if (npc.getInfo().getProfession() == Profession.Assassin) {
            return new HunterPlanner(npc);
        }
        return new DefaultPlanner(npc);
    }
}
