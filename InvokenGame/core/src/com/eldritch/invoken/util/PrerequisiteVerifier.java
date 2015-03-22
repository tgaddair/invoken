package com.eldritch.invoken.util;

import java.util.List;

import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.Inventory;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;

public abstract class PrerequisiteVerifier {
    public boolean verify(List<Prerequisite> prereqs, Agent agent) {
        for (Prerequisite prereq : prereqs) {
            // and-checking
            if (!verify(prereq, agent)) {
                return false;
            }
        }
        return true;
    }

    public boolean verify(Prerequisite prereq, Agent agent) {
        AgentInfo info = agent.getInfo();
        switch (prereq.getType()) {
            case DISCIPLINE_BETWEEN: { // TARGET attribute between MIN and MAX
                Discipline d = Discipline.valueOf(prereq.getTarget());
                int value = info.getSkillLevel(d);
                return verifyBetween(prereq, value);
            }
            case REP_BETWEEN: { // TARGET faction reputation between MIN and MAX
                int value = info.getReputation(Faction.of(prereq.getTarget()));
                return verifyBetween(prereq, value);
            }
            case RANK_BETWEEN: { // TARGET faction rank between MIN and MAX
                if (!info.hasRank(Faction.of(prereq.getTarget()))) {
                    return verifyHas(prereq, false);
                }
                int value = info.getRank(Faction.of(prereq.getTarget()));
                return verifyBetween(prereq, value);
            }
            case LVL_BETWEEN: { // player level between MIN and MAX
                int value = info.getLevel();
                return verifyBetween(prereq, value);
            }
            case ITEM_HAS: { // MIN or more of TARGET item in inventory
                int value = info.getInventory().getItemCount(prereq.getTarget());
                return verifyMin(prereq, value);
            }
            case RELATION_BETWEEN: // TARGET relation to agent
                return verifyRelation(prereq, agent);
            case ITEM_EQUIPPED: { // TARGET item is currently equipped
                Inventory inv = info.getInventory();
                boolean equipped = inv.hasItem(prereq.getTarget())
                        && inv.getItem(prereq.getTarget()).isEquipped(inv);
                return verifyHas(prereq, equipped);
            }
            case FOLLOWER: { // TARGET following agent
                Location location = agent.getLocation();
                String id = prereq.getTarget();
                boolean follower = location.hasAgentWithId(id)
                        && location.getAgentById(id).isFollowing(agent);
                return verifyHas(prereq, follower);
            }
            case INTERACTOR: { // TARGET interactor
                Location location = agent.getLocation();
                String id = prereq.getTarget();
                boolean interactor = location.hasAgentWithId(id)
                        && location.getAgentById(id) == agent;
                return verifyHas(prereq, interactor);
            }
            case STATE_MARKER: {
                Location location = agent.getLocation();
                int value = location.getMarkerCount(prereq.getTarget());
                return verifyBetween(prereq, value);
            }
            // case ACTIVE_AUG: {
            // boolean has = false;
            // for (Augmentation aug : info.getAugmentations().getAugmentations()) {
            // if (aug.getId().equals(prereq.getTarget())) {
            // has = true;
            // }
            // }
            // return verifyHas(prereq, has);
            // }
            // case MISSION_STAGE: { // TARGET mission between MIN and MAX
            // int stage = info.getMissionStage(prereq.getTarget());
            // return verifyBetween(prereq, stage);
            // }
            // case ALIVE: { // TARGET actor is alive
            // boolean alive = model.isAlive(prereq.getTarget());
            // return verifyHas(prereq, alive);
            // }
            // case STATE_MARKER: { // TARGET state marker in list
            // int value = player.getMarkerCount(prereq.getTarget());
            // return verifyBetween(prereq, value);
            // }
            default:
                throw new IllegalArgumentException("Unrecognized Prerequisite type: "
                        + prereq.getType());
        }
    }

    protected boolean verifyRelation(Prerequisite prereq, Agent agent) {
        Agent source = getSource();
        if (prereq.hasTarget()) {
            Location location = agent.getLocation();
            String id = prereq.getTarget();
            if (!location.hasAgentWithId(id)) {
                // cannot meet standing requirements, low or high, if the agent is missing
                return false;
            }
            source = location.getAgentById(id);
        } else if (source == null) {
            // missing agent
            return false;
        }

        // round down
        int value = (int) source.getRelation(agent);
        return verifyBetween(prereq, value);
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

    protected abstract Agent getSource();
}
