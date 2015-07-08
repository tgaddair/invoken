package com.eldritch.invoken.effects;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.DodgeHandler;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Jaunting extends BasicEffect {
    private static final float RADIUS = 2.5f;
    
    private final Vector2 direction = new Vector2();
	private final Vector2 target;
	private boolean arrived = false;
	
    public Jaunting(Agent agent, Vector2 target) {
        super(agent);
        this.target = target;
    }

	@Override
	public boolean isFinished() {
		return arrived || getStateTime() > 1;
	}

	@Override
	public void dispel() {
	    Agent owner = getTarget();
	    Level level = owner.getLocation();
	    Vector2 center = owner.getPosition();
	    
	    level.addEntity(AnimatedEntity.createSmokeRing(center, RADIUS * 2));
	    
	    for (Agent neighbor : owner.getNeighbors()) {
            if (neighbor.inRange(center, RADIUS)) {
                float scale = Heuristics.distanceScore(center.dst2(neighbor.getPosition()), 0);
                direction.set(neighbor.getPosition()).sub(owner.getPosition()).nor().scl(scale);
                neighbor.applyForce(direction.scl(500));
                neighbor.addEffect(new Stunned(owner, neighbor, 1f));
                level.addEntity(AnimatedEntity.createDisintegrate(neighbor.getPosition()));

                InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.MELEE_HIT,
                        neighbor.getPosition());
            }
        }
	}

	@Override
	protected void doApply() {
		Vector2 direction = target.cpy().sub(getTarget().getPosition()).nor();
    	getTarget().applyForce(direction.scl(2500));
	}

	@Override
	protected void update(float delta) {
		if (getTarget().getPosition().dst2(target) < 1) {
			getTarget().stop();
			arrived = true;
		}
	}
	
	public class JauntHandler implements DodgeHandler {
        @Override
        public boolean canDodge() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void dodge(Vector2 direction) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isFinished() {
            return Jaunting.this.isFinished();
        }
	}
}
