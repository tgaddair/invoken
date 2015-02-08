package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public class Attack extends AbstractCombatTask {
    @Override
    public void start(Npc entity) {
        fillTargets(entity);
        Agent target = selectBestTarget(entity);
        if (target != null) {
            // alert neighbors of attack
            for (Agent neighbor : entity.getNeighbors()) {
                neighbor.alertTo(target);
            }
        }
    }

    @Override
    public void run(Npc entity) {
        // update target enemy
        fillTargets(entity);
        Agent target = selectBestTarget(entity);

        // update our target
        entity.setTarget(target);
        if (target == null || !target.isAlive()) {
            // can't do anything if we are unable to find a target to attack, so wander
            fail();
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
            // entity.getHide().setEnabled(true);
        } else {
            entity.getPursue().setEnabled(shouldPursue(entity, target));
            entity.getEvade().setEnabled(shouldFlee(entity, target));
            // entity.getHide().setEnabled(true);
        }

        // attack
        attack(entity, target);
        running();
    }

    @Override
    public void end(Npc entity) {
        entity.getHide().setEnabled(false);
        entity.getPursue().setEnabled(false);
        entity.getEvade().setEnabled(false);
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

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
