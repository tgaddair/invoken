package com.eldritch.invoken.util;

import java.util.List;

import com.eldritch.invoken.actor.ActorModel;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.scifirpg.proto.Disciplines.Discipline;
import com.eldritch.scifirpg.proto.Disciplines.Influence;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.eldritch.scifirpg.proto.Effects.Effect.Range;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;

public class PrerequisiteVerifier {
    public boolean verify(List<Prerequisite> prereqs, ActorModel model) {
        boolean verified = true;
        for (Prerequisite prereq : prereqs) {
            // And-checking
            if (!verify(prereq, model)) {
                verified = false;
            }
        }
        return verified;
    }
    
    public boolean verify(Prerequisite prereq, ActorModel model) {
        Player player = model.getPlayer();
//        switch (prereq.getType()) {
//            case DISCIPLINE_BETWEEN: { // TARGET attribute between MIN and MAX
//                Discipline d = Discipline.valueOf(prereq.getTarget());
//                int value = player.getStats().getSkillLevel(d);
//                return verifyBetween(prereq, value);
//            }
//            case REP_BETWEEN: { // TARGET faction reputation between MIN and MAX
//                int value = player.getReputation(prereq.getTarget());
//                return verifyBetween(prereq, value);
//            }
//            case RANK_BETWEEN: { // TARGET faction rank between MIN and MAX
//                if (!player.hasRank(prereq.getTarget())) {
//                    return verifyHas(prereq, false);
//                }
//                int value = player.getRank(prereq.getTarget());
//                return verifyBetween(prereq, value);
//            }
//            case LVL_BETWEEN: { // player level between MIN and MAX
//                int value = player.getLevel();
//                return verifyBetween(prereq, value);
//            }
//            case STATE_MARKER: { // TARGET state marker in list
//                int value = player.getMarkerCount(prereq.getTarget());
//                return verifyBetween(prereq, value);
//            }
//            case ITEM_HAS: { // MIN or more of TARGET item in inventory
//                int value = player.getItemCount(prereq.getTarget());
//                return verifyMin(prereq, value);
//            }
//
//            // When AUG_REMAINING is a prereq, the game automatically expends the aug
//            case AUG_AVAILABLE: { // MIN magnitude TARGET augmentation effect on target in cache
//                Effect.Type effectType = Effect.Type.valueOf(prereq.getTarget());
//                int value = 0;
//                for (ActiveAugmentation action : player.getActions()) {
//                    for (Effect effect : action.getEffects()) {
//                        if (effect.getType() == effectType
//                                && effect.getRange() == Range.SELECTED) {
//                            value += effect.getMagnitude();
//                        }
//                    }
//                }
//                return verifyMin(prereq, value);
//            }
//            case INFLUENCE_AUG: { // Has an influence aug of TARGET type in cache
//                Influence influence = Influence.valueOf(prereq.getTarget());
//                boolean has = false;
//                for (ActiveAugmentation action : player.getActions()) {
//                    for (Effect effect : action.getEffects()) {
//                        if (effect.hasInfluence()
//                                && effect.getInfluence() == influence
//                                && effect.getRange() == Range.SELECTED) {
//                            has = true;
//                        }
//                    }
//                }
//                return verifyHas(prereq, has);
//            }
//            // Only has meaning within a specific DialogueTree.
//            case DIALOGUE_SEEN: // TARGET dialogue previously observed
//                // TODO maybe deprecate this
//                return true;
//            case INFLUENCE_BETWEEN: // Dialogue disposition variable between MIN and MAX
//                return verifyInfluence(prereq, player);
//            case STANDING_IS: // TARGET standing
//                return verifyStanding(prereq, player);
//
//            case ITEM_EQUIPPED: { // TARGET item is currently equipped
//                boolean equipped = player.hasEquipped(prereq.getTarget());
//                return verifyHas(prereq, equipped);
//            }
//            case MISSION_STAGE: { // TARGET mission between MIN and MAX
//                int stage = player.getMissionStage(prereq.getTarget());
//                return verifyBetween(prereq, stage);
//            }
//            case ALIVE: { // TARGET actor is alive
//                boolean alive = model.isAlive(prereq.getTarget());
//                return verifyHas(prereq, alive);
//            }
//            case FOLLOWER: { // TARGET actor is following player
//                boolean follower = player.hasFollower(prereq.getTarget());
//                return verifyHas(prereq, follower);
//            }
//            default:
//                throw new IllegalArgumentException(
//                        "Unrecognized Prerequisite type: " + prereq.getType());
//        }
        return true;
    }
    
    protected boolean verifyInfluence(Prerequisite prereq, Agent actor) {
        return true;
    }
    
    protected boolean verifyStanding(Prerequisite prereq, Agent actor) {
        return true;
    }
    
    protected final boolean verifyBetween(Prerequisite prereq, int value) {
        boolean verified = true;
        if (prereq.hasMin() && value < prereq.getMin()) {
            verified = false;
        }
        if (prereq.hasMax() && value > prereq.getMax()) {
            verified = false;
        }
        if (prereq.getNot()) {
            verified = !verified;
        }
        return verified;
    }
    
    protected final boolean verifyMin(Prerequisite prereq, int value) {
        boolean verified = true;
        if (prereq.hasMin() && value < prereq.getMin()) {
            verified = false;
        }
        if (prereq.getNot()) {
            verified = !verified;
        }
        return verified;
    }
    
    protected final boolean verifyHas(Prerequisite prereq, boolean has) {
        boolean verified = has;
        if (prereq.getNot()) {
            verified = !verified;
        }
        return verified;
    }
}
