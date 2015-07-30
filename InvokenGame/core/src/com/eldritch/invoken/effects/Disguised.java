package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;

public class Disguised extends BasicEffect {
    private final Item disguise;
    private final Faction faction;
    private final int rank;
    
    private float elapsed = 0;
	
	public Disguised(Agent target, Item disguise, Faction faction, int rank) {
	    super(target);
	    this.disguise = disguise;
	    this.faction = faction;
	    this.rank = rank;
	}

	@Override
	public boolean isFinished() {
		return !disguise.isEquipped(target.getInventory()) || !target.isAlive();
	}

	@Override
	public void dispel() {
	    // reset the true rank
	}
	
	@Override
    protected void doApply() {
	    // set the target's perceived rank in the faction
	    
	    // for every member of the faction, invalidate their cached relation to the target
	    
	    // recalculate member reactions
    }

    @Override
    protected void update(float delta) {
    }
}
