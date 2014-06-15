package com.eldritch.invoken.actor.ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public abstract class AttackRoutine extends MovementRoutine {
    private final Set<Agent> targets = new HashSet<Agent>();
    private Agent target = null;
    private float elapsed = 0;

    public AttackRoutine(Npc npc, Location location) {
        super(npc, location);
    }

    @Override
    public boolean isFinished() {
        return !isValid();
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    @Override
    public void reset() {
        elapsed = 0;
        target = null;
    }

    protected abstract void fillTargets(Collection<Agent> targets);

    @Override
    public void takeAction(float delta, Location screen) {
        // update target enemy
        fillTargets(targets);
        target = selectTarget();

        // update our target
        npc.setTarget(target);
        if (target == null || !target.isAlive()) {
            // can't do anything if we are unable to find a target to attack
            return;
        }

        // melee attackers don't avoid obstacles as fiercely
        move(delta, screen, !npc.getInventory().hasMeleeWeapon());
        attack(delta, screen);
    }

    private Agent selectTarget() {
        // get one of our enemies
        Agent current = null;
        float bestDistance = Float.MAX_VALUE;
        for (Agent agent : targets) {
            if (!agent.isAlive()) {
                // no point in attacking a dead enemy
                continue;
            }

            float distance = npc.dst2(agent);
            if (current == null || distance < bestDistance) {
                // attack the closer enemy
                current = agent;
                bestDistance = distance;
            }
        }
        return current;
    }

    private void attack(float delta, Location location) {
        elapsed += delta;
        if (npc.getTarget() == null || !npc.canTarget(location)) {
            // can't attack invalid targets
            return;
        }

        if (!npc.hasPendingAction() && elapsed >= 1) {
            // choose the aug with the highest situational quality score
            Augmentation chosen = null;
            for (Augmentation aug : npc.getInfo().getAugmentations().getAugmentations()) {
                if (aug.isValid(npc, npc.getTarget())
                        && aug.quality(npc, npc.getTarget(), location) > 0) {
                    chosen = aug;
                }
            }

            // if an aug was chosen, then go ahead and use it
            if (chosen != null) {
                npc.getInfo().getAugmentations().use(chosen);
                elapsed = 0;
            }
        }
    }

    @Override
    protected void doMove(Vector2 velocityDelta, Location location) {
        if (shouldPursue()) {
            pursueTarget(npc.getClearTarget(location), velocityDelta, location);
        } else if (shouldFlee()) {
            fleeTarget(getTargetPosition(), velocityDelta, location);
        } else {
            // side strafe to avoid attack
            // pursueTarget(npc.getClearTarget(Math.PI / 2, location), velocityDelta, location);
        }
    }

    public Vector2 getTargetPosition() {
        return target.getPosition();
    }

    private boolean shouldPursue() {
        // don't wait till we've lost them in our sights
        float maxDistance = npc.getInfo().getMaxTargetDistance() * 0.8f;
        if (npc.getInventory().hasMeleeWeapon()) {
            // get in closer when a melee weapon is equipped
            maxDistance = 1.5f;
        }

        return getTargetPosition().dst2(npc.getPosition()) >= maxDistance;
    }

    private boolean shouldFlee() {
        // don't get any closer to the enemy than this
        float minDistance = npc.getInfo().getMaxTargetDistance() * 0.4f;
        if (npc.getInventory().hasMeleeWeapon()) {
            // get in closer when a melee weapon is equipped
            minDistance = 1;
        }
        return getTargetPosition().dst2(npc.getPosition()) <= minDistance;
    }
}
