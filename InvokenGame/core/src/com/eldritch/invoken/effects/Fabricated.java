package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.location.Level;

public class Fabricated extends BasicEffect {
	private final String id;
	private final Vector2 point;
	private final float cost;
	
	private Agent fabricant = null;
	
	public Fabricated(Agent owner, String id, Vector2 point, float cost) {
	    super(owner);
	    this.id = id;
	    this.point = point;
		this.cost = cost;
	}

	@Override
	public boolean isFinished() {
		return fabricant != null && !fabricant.isAlive();
	}

	@Override
	public void dispel() {
		target.getInfo().changeMaxEnergy(cost);
	}
	
	@Override
    protected void doApply() {
        Level level = target.getLocation();
		fabricant = Npc.create(InvokenGame.ACTOR_READER.readAsset(id), point.x, point.y, level);
		level.addAgent(fabricant);
		
		target.addFollower(fabricant);
		target.getInfo().changeMaxEnergy(-cost);
    }

    @Override
    protected void update(float delta) {
    }
}
