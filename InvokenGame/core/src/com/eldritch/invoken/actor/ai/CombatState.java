package com.eldritch.invoken.actor.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;
import com.eldritch.invoken.location.Level;

public enum CombatState implements State<Npc> {
	ATTACK() {
		private final List<Agent> targets = new ArrayList<Agent>();
		
		@Override
		public void enter(Npc entity) {
		    fillTargets(entity, targets);
            Agent target = selectBestTarget(entity, targets);
            if (target != null) {
                // alert neighbors of attack
                for (Agent neighbor : entity.getNeighbors()) {
                    neighbor.alertTo(target);
                }
            }
		}

		@Override
		public void update(Npc entity) {
			// update target enemy
	        fillTargets(entity, targets);
	        Agent target = selectBestTarget(entity, targets);
	        
	        // update our target
	        entity.setTarget(target);
	        if (target == null || !target.isAlive()) {
	            // can't do anything if we are unable to find a target to attack, so wander
	            entity.getStateMachine().changeState(NpcState.COMBAT, HUNT);
	            return;
	        }
	        
	        // enable/disable movement behaviors
	        entity.getHide().setTarget(target);
	        entity.getHide().setEnabled(true);
	        entity.getPursue().setTarget(target);
	        entity.getEvade().setTarget(target);
	        if (entity.getInventory().hasMeleeWeapon()) {
	            entity.getPursue().setEnabled(true);
	            entity.getEvade().setEnabled(false);
	            entity.getHide().setEnabled(false);
	        } else if (!entity.canAttack()) {
	        	entity.getPursue().setEnabled(false);
	            entity.getEvade().setEnabled(true);
//	            entity.getHide().setEnabled(true);
	        } else {
	        	entity.getPursue().setEnabled(shouldPursue(entity, target));
	        	entity.getEvade().setEnabled(shouldFlee(entity, target));
//	        	entity.getHide().setEnabled(true);
	        }
	        
	        // attack
	        attack(entity, target);
		}
		
		@Override
		protected void afterExit(Npc entity) {
//			entity.getHide().setEnabled(false);
			entity.getPursue().setEnabled(false);
			entity.getEvade().setEnabled(false);
		}
		
		private void attack(Npc npc, Agent target) {
			Level level = npc.getLocation();
			if (!npc.canTarget(target, level)) {
	            // can't attack invalid targets
	            return;
	        }
			
			if (!npc.hasPendingAction() && !npc.actionInProgress()) {
	            // choose the aug with the highest situational quality score
	            Augmentation chosen = null;
	            float bestQuality = 0; // never choose an aug with quality <= 0
	            for (Augmentation aug : npc.getInfo().getAugmentations().getAugmentations()) {
	                if (aug.hasEnergy(npc) && aug.isValid(npc, npc.getTarget())) {
	                    float quality = aug.quality(npc, npc.getTarget(), level);
	                    if (quality > bestQuality) {
	                        chosen = aug;
	                        bestQuality = quality;
	                    }
	                }
	            }

	            // if an aug was chosen, then go ahead and use it
	            if (chosen != null) {
	                npc.getInfo().getAugmentations().prepare(chosen);
	                npc.getInfo().getAugmentations().use(chosen);
	                npc.setCanAttack(true);
	            } else {
	            	// cannot use any aug, so we need to start evading
	            	npc.setCanAttack(false);
	            }
	        }
		}
		
		private boolean shouldPursue(Npc npc, Agent target) {
	        // don't wait till we've lost them in our sights
	        float maxDistance = npc.getInfo().getMaxTargetDistance() * 0.8f;
	        if (npc.getInventory().hasMeleeWeapon()) {
	            // get in closer when a melee weapon is equipped
	            maxDistance = 1.25f;
	        }

	        return target.getPosition().dst2(npc.getPosition()) >= maxDistance;
	    }

	    private boolean shouldFlee(Npc npc, Agent target) {
	        // don't get any closer to the enemy than this
	        float minDistance = npc.getInfo().getMaxTargetDistance() * 0.4f;
	        if (npc.getInventory().hasMeleeWeapon()) {
	            // get in closer when a melee weapon is equipped
	            minDistance = 1;
	        }
	        return target.getPosition().dst2(npc.getPosition()) <= minDistance;
	    }
	},
	
	HUNT() {
	    private final List<Agent> targets = new ArrayList<Agent>();
        
        @Override
        public void enter(Npc entity) {
            entity.getSeek().setTarget(entity.getLastSeen());
            entity.getSeek().setEnabled(true);
            entity.getHide().setTarget(entity.getLastSeen());
            entity.getHide().setEnabled(true);
        }

        @Override
        public void update(Npc entity) {
            Vector2 lastSeen = entity.getLastSeen().getPosition();
            
            // update target enemy
            fillTargets(entity, targets);
            Agent target = selectBestTarget(entity, targets);
            
            // update our target
            entity.setTarget(target);
            if (target != null && target.isAlive()) {
                // can't do anything if we are unable to find a target to attack, so wander
                entity.getStateMachine().changeState(NpcState.COMBAT, ATTACK);
            } else if (entity.getPosition().dst2(lastSeen) < 1) {
                entity.getStateMachine().changeState(NpcState.COMBAT, WANDER);
            }
        }
        
        @Override
        protected void afterExit(Npc entity) {
            entity.getSeek().setEnabled(false);
        }
	},
	
	WANDER() {
	    private final List<Agent> targets = new ArrayList<Agent>();
	    
        @Override
        public void enter(Npc entity) {
            entity.getWander().setEnabled(true);
        }

        @Override
        public void update(Npc entity) {
            Vector2 lastSeen = entity.getLastSeen().getPosition();
            
            // update target enemy
            fillTargets(entity, targets);
            Agent target = selectBestTarget(entity, targets);
            
            // update our target
            entity.setTarget(target);
            if (target != null && target.isAlive()) {
                // can't do anything if we are unable to find a target to attack, so wander
                entity.getStateMachine().changeState(NpcState.COMBAT, ATTACK);
            } else if (entity.getPosition().dst2(lastSeen) > 5 && entity.hasLineOfSight(lastSeen)) {
                entity.getStateMachine().changeState(NpcState.COMBAT, HUNT);
            }
        }
        
        @Override
        protected void afterExit(Npc entity) {
            entity.getWander().setEnabled(false);
        }
    },
	
	ASSIST() {
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
		}
	},
	
	FLEE() {
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
		}
	},
	
	HIDE() {
	    private final List<Agent> targets = new ArrayList<Agent>();
        
        @Override
        public void enter(Npc entity) {
            entity.setBehavior(SteeringMode.Evade);
//            entity.getHide().setTarget(entity.getLastSeen());
            entity.getHide().setTarget(entity.getLocation().getPlayer());
            entity.getHide().setEnabled(true);
            entity.getEvade().setTarget(entity.getLocation().getPlayer());
            entity.getEvade().setEnabled(true);
        }

        @Override
        public void update(Npc entity) {
            // update target enemy
            fillTargets(entity, targets);
            Agent target = selectBestTarget(entity, targets);
            
            // update our target
            entity.setTarget(target);
            if (target != null && target.isAlive()) {
//                entity.getHide().setTarget(target);
//                entity.getEvade().setTarget(target);
            } else {
//                entity.getHide().setTarget(entity.getLastSeen());
//                entity.getEvade().setTarget(entity.getLastSeen());
            }
            entity.getEvade().setEnabled(entity.hasLineOfSight(entity.getLocation().getPlayer()));
        }
        
        @Override
        protected void afterExit(Npc entity) {
            entity.setBehavior(null);
            entity.getHide().setEnabled(false);
            entity.getEvade().setEnabled(false);
        }
	};

	@Override
	public void update(Npc entity) {
	}

	@Override
	public void exit(Npc entity) {
		entity.getStateMachine().resetValidator();
		afterExit(entity);
	}

	@Override
	public boolean onMessage(Npc entity, Telegram telegram) {
		return false;
	}
    
    protected final void fillTargets(Npc entity, Collection<Agent> targets) {
        targets.clear();
        entity.getBehavior().getAssaultTargets(entity.getVisibleNeighbors(), targets);
    }
    
    protected final Agent selectBestTarget(Npc entity, Collection<Agent> targets) {
        // get one of our enemies
        Agent current = null;
        float bestDistance = Float.MAX_VALUE;
        for (Agent agent : targets) {
            if (!agent.isAlive()) {
                // no point in attacking a dead enemy
                continue;
            }

            float distance = entity.dst2(agent);
            if (current == null || distance < bestDistance) {
                // attack the closer enemy
                current = agent;
                bestDistance = distance;
            }
        }
        return current;
    }
	
    protected void afterExit(Npc entity) {
    }
}
