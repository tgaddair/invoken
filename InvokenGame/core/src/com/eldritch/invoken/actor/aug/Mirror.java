package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Mirroring;
import com.eldritch.invoken.location.Level;

public class Mirror extends SelfAugmentation {
	private static class Holder {
        private static final Mirror INSTANCE = new Mirror();
	}
	
	public static Mirror getInstance() {
		return Holder.INSTANCE;
	}
	
    private Mirror() {
        super("mirror");
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return new MirrorAction(owner);
    }
    
    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new MirrorAction(owner);
    }
    
    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }
	
	@Override
    public int getCost(Agent owner) {
        return owner.isToggled(Mirroring.class) ? 0 : 5;
    }
	
	@Override
    public float quality(Agent owner, Agent target, Level level) {
        return 1;
    }
	
	public class MirrorAction extends AnimatedAction {
		public MirrorAction(Agent actor) {
			super(actor, Activity.Cast, Mirror.this);
		}

		@Override
		public void apply(Level level) {
			if (owner.toggle(Mirroring.class)) {
				owner.addEffect(new Mirroring(owner, Mirror.this));
			}
		}
		
		@Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
	}
}
