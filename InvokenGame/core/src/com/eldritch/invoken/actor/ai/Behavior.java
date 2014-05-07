package com.eldritch.invoken.actor.ai;

import java.util.Collection;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Confidence;

public class Behavior {
    private final Npc npc;
    private final NonPlayerActor baseData;
    private Aggression aggression;
    private Assistance assistance;
    private Confidence confidence;
    
    public Behavior(Npc npc, NonPlayerActor actor) {
        this.npc = npc;
        baseData = actor;
        aggression = actor.getAggression();
        assistance = actor.getAssistance();
        confidence = actor.getConfidence();
    }
    
    public boolean shouldFlee(Collection<Agent> actors) {
        if (aggression == Aggression.FRENZIED) {
            // frenzied NPCs never flee
            return false;
        }
        if (!npc.getEnemies().isEmpty() && confidence == Confidence.COWARDLY) {
            // cowardly NPCs that are not frenzied flee before enemies
            return true;
        }
        // TODO: others flee when their health gets too low
        for (Agent enemy : npc.getEnemies()) {
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
        for (Agent enemy : npc.getEnemies()) {
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
            return true;
        }
        for (Agent agent : actors) {
            if (npc.hostileTo(agent) || wantsToAttack(agent)) {
                // we have reason to attack this actor, so we should assault
                if (willingToAttack(agent)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void getAssaultTargets(Collection<Agent> actors, Collection<Agent> targets) {
        for (Agent agent : actors) {
            if (npc.hostileTo(agent) || wantsToAttack(agent)) {
                if (willingToAttack(agent)) {
                    targets.add(agent);
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
            if (npc.hostileTo(agent) || wantsToAttack(agent)) {
                if (shouldAssistAgainst(agent, actors)) {
                    if (willingToAttack(agent)) {
                        targets.add(agent);
                    }
                }
            }
        }
    }
    
    private boolean shouldAssistAgainst(Agent enemy, Collection<Agent> actors) {
        if (enemy.getEnemies().isEmpty()) {
            // not in combat, so no need to assist someone attacking them
            return false;
        }
        return obligatedToAttack(enemy, actors);
    }
    
    /**
     * Returns true if we will attack other given all the actors in the scene.
     */
    public boolean willAttack(Agent other, Collection<Agent> actors) {
        if (aggression == Aggression.FRENZIED) {
            // frenzied NPCs always attack anyone
            return true;
        }
        if (confidence == Confidence.COWARDLY) {
            // cowardly NPCs that are not frenzied never attack anyone
            return false;
        }
        if (npc.hostileTo(other) && aggression != Aggression.PACIFIST) {
            // if they attacked us, and we're not a pacifist, then attack them back
            return true;
        }
        
        // we're not a coward and we haven't been attack by this actor
        if (wantsToAttack(other) || obligatedToAttack(other, actors)) {
            if (willingToAttack(other)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if our aggression dictates we should attack given our reaction.
     */
    private boolean wantsToAttack(Agent other) {
        float reaction = npc.getDisposition(other);
        if (isEnemyGiven(reaction)) {
            if (aggression.ordinal() >= Aggression.AGGRESSIVE_VALUE) {
                // aggressive actors attack enemies on sight
                return true;
            }
        }
        if (isNeutralGiven(reaction)) {
            if (aggression.ordinal() >= Aggression.HOSTILE_VALUE) {
                // hostile actors attack non-allies on sight
                return true;
            }
        }
        if (isAllyGiven(reaction)) {
            if (aggression.ordinal() >= Aggression.FRENZIED_VALUE) {
                // frenzied actors attack anyone on sight
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if our assistance dictates we should attack given our reaction.
     */
    private boolean obligatedToAttack(Agent other, Collection<Agent> actors) {
        if (assistance == Assistance.DETACHED) {
            // detached actors never assist
            return false;
        }
        
        // we're at least loyal to our allies, so consider the situation
        float otherReaction = npc.getDisposition(other);
        for (Agent agent : actors) {
            if (agent.hostileTo(other)) {
                float reaction = npc.getDisposition(agent);
                if (isAllyGiven(reaction) && reaction > otherReaction) {
                    // an ally of ours is attacking other, and we like this ally more than
                    // we like other
                    return true;
                } else if (!isEnemyGiven(reaction) && reaction > otherReaction) {
                    // we at least like this character more than the other one
                    if (assistance == Assistance.CHIVALRIC) {
                        // chivalric actors assist non-enemies
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if our confidence dictates we should attack given our level.
     */
    private boolean willingToAttack(Agent other) {
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
    
    private boolean isEnemyGiven(float reaction) {
        return reaction <= -10;
    }
    
    private boolean isNeutralGiven(float reaction) {
        return !isEnemyGiven(reaction) && !isAllyGiven(reaction);
    }
    
    private boolean isAllyGiven(float reaction) {
        return reaction >= 10;
    }
}
