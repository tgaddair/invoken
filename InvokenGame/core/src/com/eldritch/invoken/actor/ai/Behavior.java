package com.eldritch.invoken.actor.ai;

import java.util.Collection;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Confidence;

public class Behavior {
    private final Npc npc;
    private final NonPlayerActor baseData;
    private Aggression aggression;
    private Assistance assistance;
    private Confidence confidence;
    
    // counters for handling multiple applications of the same effect
    private int aggressionCounter = 0;
    
    public Behavior(Npc npc, NonPlayerActor actor) {
        this.npc = npc;
        baseData = actor;
        aggression = actor.getAggression();
        assistance = actor.getAssistance();
        confidence = actor.getConfidence();
    }
    
    public void setAggression(Aggression aggression) {
        aggressionCounter++;
        this.aggression = aggression;
    }
    
    public void resetAggression() {
        aggressionCounter--;
        if (aggressionCounter <= 0) {
            this.aggression = baseData.getAggression();
        }
    }
    
    public Aggression getAggression() {
        return aggression;
    }
    
    public boolean shouldFlee(Collection<Agent> actors) {
        if (aggression == Aggression.FRENZIED) {
            // frenzied NPCs never flee
            return false;
        }
        if (npc.getThreat().hasEnemies() && confidence == Confidence.COWARDLY) {
            // cowardly NPCs that are not frenzied flee before enemies
            return true;
        }
        // TODO: others flee when their health gets too low
        for (Agent enemy : npc.getThreat().getEnemies()) {
            if (!willingToAttack(enemy)) {
                // if someone's hostile, and we're not willing to attack, then flee
                return true;
            }
        }
        
        // finally, we should also flee if one of our allies in combat and we cannot help
        for (Agent agent : actors) {
            if (shouldAssistAgainst(agent, actors)) {
                if (!willingToAttack(agent)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void getFleeTargets(Collection<Agent> actors, Collection<Agent> targets) {
        for (Agent enemy : npc.getThreat().getEnemies()) {
            if (!willingToAttack(enemy)) {
                targets.add(enemy);
            }
        }
        for (Agent agent : actors) {
            if (shouldAssistAgainst(agent, actors)) {
                if (!willingToAttack(agent)) {
                    targets.add(agent);
                }
            }
        }
    }
    
    public boolean shouldAssault(Collection<Agent> actors) {
        // assume we should not flee, we check that first
        if (aggression == Aggression.FRENZIED) {
            // frenzied NPCs always attack anyone
            return !actors.isEmpty();
        }
        for (Agent agent : actors) {
            if (agent.isAlive()) {
                if (npc.getThreat().hostileTo(agent) || wantsToAttack(agent)) {
                    // we have reason to attack this actor, so we should assault
                    if (willingToAttack(agent)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void getAssaultTargets(Collection<Agent> actors, Collection<Agent> targets) {
        for (Agent agent : actors) {
        	if (npc.hasLineOfSight(agent)) {
        		// only target if we have line of sight
        		if (npc.getThreat().hostileTo(agent) || wantsToAttack(agent)) {
                    if (willingToAttack(agent)) {
                        targets.add(agent);
                    }
                }
        	}
        }
    }
     
    public boolean shouldAssist(Collection<Agent> actors) {
        // assumes we should neither flee nor assault
        if (assistance == Assistance.DETACHED) {
            // never assist
            return false;
        }
        for (Agent agent : actors) {
            if (shouldAssistAgainst(agent, actors)) {
                // we are obligated to attack this character given our assistance
                if (willingToAttack(agent)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void getAssistTargets(Collection<Agent> actors, Collection<Agent> targets) {
        for (Agent agent : actors) {
            if (shouldAssistAgainst(agent, actors)) {
                if (willingToAttack(agent)) {
                    targets.add(agent);
                }
            }
        }
    }
    
    private boolean shouldAssistAgainst(Agent enemy, Collection<Agent> actors) {
        if (!enemy.getThreat().hasEnemies()) {
            // not in combat, so no need to assist someone attacking them
            return false;
        }
        return obligatedToAttack(enemy, actors);
    }
    
    /**
     * Returns true if our aggression dictates we should attack given our reaction.
     */
    public boolean wantsToAttack(Agent other) {
        return wantsToAttack(other, false);
    }
    
    public boolean wantsToAttack(Agent other, boolean alerted) {
        if (aggression == Aggression.FRENZIED) {
            // frenzied actors attack anyone on sight
            return true;
        }
        
        float reaction = npc.getRelation(other);
        if (isEnemyGiven(reaction)) {
            if (aggression.ordinal() >= Aggression.AGGRESSIVE_VALUE || npc.assaultedBy(other)) {
                // aggressive actors attack enemies on sight
                // we also want to attack if we've been assaulted by an enemy
                return true;
            } else if (aggression.ordinal() >= Aggression.UNAGGRESSIVE_VALUE && alerted) {
                // we're alerted to this enemy, so even though we're unaggressive, we see this as
                // an opportunity to strike; note that passive NPCs will still sit this one out
                return true;
            }
        }
        if (isNeutralGiven(reaction)) {
            if (aggression.ordinal() >= Aggression.HOSTILE_VALUE) {
                // hostile actors attack non-allies on sight
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if our assistance dictates we should attack given our reaction.
     */
    private boolean obligatedToAttack(Agent target, Collection<Agent> actors) {
        if (assistance == Assistance.DETACHED) {
            // detached actors never assist
            return false;
        }
        
        // we're at least loyal to our allies, so consider the situation
        float targetReaction = npc.getRelation(target);
        for (Agent agent : actors) {
            if (agent.getThreat().hostileTo(target)) {
                if (shouldAssist(agent, target, targetReaction)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean shouldAssist(Agent agent, Agent target) {
        return shouldAssist(agent, target, npc.getRelation(target));
    }
    
    private boolean shouldAssist(Agent agent, Agent target, float targetReaction) {
        float reaction = npc.getRelation(agent);
        if (isAllyGiven(reaction) && !isAllyGiven(targetReaction)) {
            // support allies against non-allies
            return true;
        } else if (agent.assaultedBy(target)) {
            // assist allies when attacked
            if (isAllyGiven(reaction)) {
                return true;
            }
            
            // chivalric agents assist non-enemies when they're attacked
            if (assistance == Assistance.CHIVALRIC || !isEnemyGiven(reaction)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if our confidence dictates we should attack given our level.
     */
    private boolean willingToAttack(Agent other) {
        if (aggression == Aggression.FRENZIED) {
            // frenzied actors attack anyone on sight
            return true;
        }
        
        switch (confidence) {
            case COWARDLY:
                // cowards never attack
                return false;
            case CAUTIOUS:
                // cautious attack those lower level than themselves
                return npc.getInfo().getLevel() > other.getInfo().getLevel() + 5;
            case CAPABLE:
                // capable attack those not higher level than themselves
                return npc.getInfo().getLevel() + 5 > other.getInfo().getLevel();
            case BRAVE:
            case RECKLESS:
                // brave and reckless always attack
                return true;
            default:
                throw new IllegalArgumentException("Unrecognized Confidence: " + confidence);
        }
    }
    
    public static boolean isEnemyGiven(float reaction) {
        return reaction <= -30;
    }
    
    public static boolean isNeutralGiven(float reaction) {
        return !isEnemyGiven(reaction) && !isAllyGiven(reaction);
    }
    
    public static boolean isAllyGiven(float reaction) {
        return reaction >= 30;
    }
}
