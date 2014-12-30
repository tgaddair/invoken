package com.eldritch.invoken.actor.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public enum CombatState implements State<Npc> {
	ATTACK() {
		private final List<Agent> targets = new ArrayList<Agent>();
		
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
			// update target enemy
	        List<Agent> targets = fillTargets(entity);
	        Agent target = selectBestTarget(entity, targets);
	        
	        // update our target
	        entity.setTarget(target);
	        if (target == null || !target.isAlive()) {
	            // can't do anything if we are unable to find a target to attack
	            return;
	        }
	        
	        // enable/disable movement behaviors
	        entity.getSeek().setTarget(target);
	        entity.getFlee().setTarget(target);
	        if (entity.getInventory().hasMeleeWeapon()) {
	            entity.getSeek().setEnabled(true);
	            entity.getFlee().setEnabled(false);
	        } else {
	        	entity.getSeek().setEnabled(shouldPursue(entity, target));
	        	entity.getFlee().setEnabled(shouldFlee(entity, target));
	        }
	        
	        // attack
	        attack(entity, target);
		}
		
		private void attack(Npc npc, Agent target) {
			Location location = npc.getLocation();
			if (!npc.canTarget(target, location)) {
	            // can't attack invalid targets
	            return;
	        }
			
			if (!npc.hasPendingAction() && !npc.actionInProgress()) {
	            // choose the aug with the highest situational quality score
	            Augmentation chosen = null;
	            float bestQuality = 0; // never choose an aug with quality <= 0
	            for (Augmentation aug : npc.getInfo().getAugmentations().getAugmentations()) {
	                if (aug.hasEnergy(npc) && aug.isValid(npc, npc.getTarget())) {
	                    float quality = aug.quality(npc, npc.getTarget(), location);
	                    if (quality > bestQuality) {
	                        chosen = aug;
	                        bestQuality = quality;
	                    }
	                }
	            }

	            // if an aug was chosen, then go ahead and use it
	            if (chosen != null) {
	                npc.getInfo().getAugmentations().use(chosen);
	            }
	        }
		}
		
		@Override
		public void exit(Npc entity) {
			entity.getSeek().setEnabled(false);
			entity.getFlee().setEnabled(false);
		}
		
		private List<Agent> fillTargets(Npc entity) {
	        targets.clear();
	        entity.getBehavior().getAssaultTargets(entity.getNeighbors(), targets);
	        return targets;
	    }
		
		private Agent selectBestTarget(Npc entity, Collection<Agent> targets) {
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
		@Override
		public void enter(Npc entity) {
		}

		@Override
		public void update(Npc entity) {
		}
	};

	@Override
	public void update(Npc entity) {
	}

	@Override
	public void exit(Npc entity) {
		entity.getStateMachine().resetValidator();
	}

	@Override
	public boolean onMessage(Npc entity, Telegram telegram) {
		return false;
	}
}
