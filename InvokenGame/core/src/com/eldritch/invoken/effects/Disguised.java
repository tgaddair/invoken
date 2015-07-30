package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.factions.FactionManager;
import com.eldritch.invoken.actor.factions.FactionManager.PerceivedRank;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.items.Outfit.Disguise;
import com.eldritch.invoken.actor.type.Agent;

public class Disguised extends BasicEffect implements PerceivedRank, Disguise {
    private final Item disguise;
    private final Faction faction;
    private final int rank;

    private boolean valid = true;

    public Disguised(Agent target, Item disguise, Faction faction, int rank) {
        super(target);
        this.disguise = disguise;
        this.faction = faction;
        this.rank = rank;
    }

    @Override
    public boolean isFinished() {
        return !valid || !disguise.isEquipped(target.getInventory()) || !target.isAlive();
    }

    @Override
    protected void doApply() {
        FactionManager manager = target.getInfo().getFactionManager();

        // set the target's perceived rank in the faction
        target.setDisguised(this);
        manager.setPerceivedRank(faction, this);

        // for every member of the faction, update their cached relation to the target
        for (Agent member : faction.getMembers()) {
            member.updateDisposition(target);
        }
    }

    @Override
    public void dispel() {
        FactionManager manager = target.getInfo().getFactionManager();

        // reset the true rank
        target.removeDisguise(this);
        manager.removePerceivedRank(faction, this);

        // for every member of the faction, update their cached relation to the target
        for (Agent member : faction.getMembers()) {
            member.updateDisposition(target);
        }
    }

    @Override
    protected void update(float delta) {
    }

    @Override
    public int getRank() {
        return rank;
    }
    
    @Override
    public void invalidate() {
        valid = false;
    }
}
