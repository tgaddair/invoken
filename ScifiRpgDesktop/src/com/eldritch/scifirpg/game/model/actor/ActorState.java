package com.eldritch.scifirpg.game.model.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.eldritch.scifirpg.game.model.ActiveAugmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Effects.DamageType;
import com.eldritch.scifirpg.proto.Effects.Effect;

public class ActorState implements Comparable<ActorState> {
    private final Actor actor;
    private int actions;
    
    // Combat stats reset after new encounter begins
    private final Set<ActorState> enemies = new HashSet<>();
    private final Set<ActiveEffect> activeEffects = new HashSet<>();
    private final Map<Augmentation.Type, ActiveAugmentation> counters = new HashMap<>();
    private int health;
    
    public ActorState(Actor actor) {
        this.actor = actor;
        health = actor.getBaseHealth();
    }
    
    public void startTurn() {
        actions = actor.getActionsPerTurn();
        
        // Elapse, remove, and apply all status effects
        
        // Elapse all counters
    }
    
    public void markActionTaken(Action action) {
        actions--;
    }
    
    public void addEffect(ActiveEffect effect) {
        activeEffects.add(effect);
    }
    
    public void applyActiveEffects() {
        Iterator<ActiveEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            ActiveEffect effect = it.next();
            effect.apply();
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }
    
    public void applyEffect(Effect effect) {
        /*
        if (checkSuccess(effect)) {
            if (effect.isDispel()) {
                removeEffects(effect);
            } else {
                addEffect(effect);
            }
        }
        */
    }
    
    public boolean canTakeAnyAction() {
        if (isParalyzed()) {
            return false;
        }
        if (isAsleep()) {
            return false;
        }
        return true;
    }
    
    public boolean canTakeAction(Action action) {
        if (action.isAttack() && isSuppressed()) {
            return false;
        }
        if (action.isInvoke() && isQuiescent()) {
            return false;
        }
        if (action.isDeceive() && isDetected()) {
            return false;
        }
        if (action.isInfluence() && isSilenced()) {
            return false;
        }
        return true;
    }
    
    public boolean isAsleep() {
        return false;
    }
    
    public boolean isParalyzed() {
        return false;
    }
    
    public boolean isSuppressed() {
        return false;
    }
    
    public boolean isQuiescent() {
        return false;
    }
    
    public boolean isDetected() {
        return false;
    }
    
    public boolean isSilenced() {
        return false;
    }
    
    public boolean isInsane() {
        return false;
    }
    
    public boolean checkPanicked() {
        return isInsane(); // Also check for random chance
    }
    
    public Action randomAction() {
        // TODO
        return null;
    }
    
    public Actor getActor() {
        return actor;
    }
    
    public int getInitiative() {
        return actor.getInitiative();
    }
    
    public boolean checkSuccess(Action action) {
        if (action.isAttack()) {
            return handleAttack(action);
        } else if (action.isDeceive()) {
            return handleDeceive(action);
        } else if (action.isInvoke()) {
            return handleExecute(action);
        } else if (action.isInfluence()) {
            // TODO
            return true;
        } else {
            throw new IllegalArgumentException("Unrecognized action type!");
        }
    }

    /**
     * Returns true if the attack succeeds
     */
    public boolean handleAttack(Action action) {
        ActorState a = action.getActor();
        //maybeCounter(Augmentation.Type.ATTACK, a, combatants);
        enemies.add(a);
        
        double weaponAccuracy = 1.0; //source.getWeaponAccuracy(action);
        double chance = a.getAccuracy() * weaponAccuracy * (1.0 - getDefense());
        boolean success = Math.random() < chance;
        return success;
    }
    
    public double getAccuracy() {
        return 0.75 + (actor.getWarfare() / 100.0);
    }
    
    public double getDefense() {
        return Math.min(actor.getWarfare() / 100.0, 1.0);
    }
    
    /**
     * Returns true if the deception succeeds
     */
    public boolean handleDeceive(Action action) {
        ActorState a = action.getActor();
        //maybeCounter(Augmentation.Type.DECEIVE, a, combatants);
        double chance = a.getDeception() * (1.0 - getPerception());
        boolean success = Math.random() < chance;
        return success;
    }
    
    public double getDeception() {
        return 0.5 + (actor.getSubterfuge() / 100.0);
    }
    
    public double getPerception() {
        return Math.min((getAlertness() + actor.getSubterfuge()) / 100.0, 1.0);
    }
    
    public int getAlertness() {
        // TODO
        // return location.getCommotion() * getWarfare();
        return 0;
    }

    /**
     * Returns true if the execution succeeds
     */
    public boolean handleExecute(Action action) {
        ActorState a = action.getActor();
        //maybeCounter(Augmentation.Type.EXECUTE, a, combatants);
        
        // Unlike other abilities, can execute on self, so ignore resistance
        double chance = a.getWillpower();
        if (a != this) {
            chance *= 1.0 - getResistance();
        }
        
        boolean success = Math.random() < chance;
        return success;
    }
    
    public double getWillpower() {
        return 0.5 + (actor.getAutomata() / 100.0);
    }
    
    public double getResistance() {
        return Math.min(actor.getAutomata() / 100.0, 1.0);
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public int getCurrentHealth() {
        return health;
    }
    
    public int getInjuries() {
        return actor.getBaseHealth() - getCurrentHealth();
    }

    public void changeHealth(int magnitude) {
        if (magnitude >= 0) {
            heal(magnitude);
        } else {
            damage(magnitude);
        }
    }
    
    public int damage(DamageType type, int magnitude) {
        // TODO handle resistances
        return damage(magnitude);
    }

    public int damage(int magnitude) {
        // Can't do more damage than the target has health
        int damage = Math.min(magnitude, health);
        health -= damage;
        System.out.println(actor.getName() + ": " + health);
        return damage;
    }
    
    public int heal(int magnitude) {
        // Can't heal more than our maximum health
        int value = Math.min(magnitude, actor.getBaseHealth() - health);
        health = value;
        return value;
    }
    
    public Collection<ActorState> getEnemies() {
        return enemies;
    }
    
    public boolean hasEnemy() {
        boolean found = false;
        Iterator<ActorState> it = enemies.iterator();
        while (it.hasNext()) {
            // TODO recalculate aggression maybe?
            ActorState actor = it.next();
            if (actor.isAlive()) {
                found = true;
            } else {
                it.remove();
            }
        }
        return found;
    }
    
    public boolean hasEnemy(ActorState actor) {
        return enemies.contains(actor);
    }

    @Override
    public int compareTo(ActorState other) {
        // Descending order by initiative
        return Integer.compare(other.getInitiative(), this.getInitiative());
    }
}
