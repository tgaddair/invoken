package com.eldritch.scifirpg.game.model.actor;

import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.eldritch.scifirpg.proto.Effects.Effect.Type;

public abstract class ActiveEffect {
    protected final Effect effect;
    protected final ActorState source;
    protected final ActorState target;
    private int remaining;
    
    public ActiveEffect(Effect effect, ActorState source, ActorState target) {
        this.effect = effect;
        this.source = source;
        this.target = target;
        this.remaining = effect.hasDuration() ? effect.getDuration() : 0;
    }
    
    public ActorState getTarget() {
        return target;
    }
    
    public int getMagnitude() {
        return effect.getMagnitude();
    }
    
    public Type getType() {
        return effect.getType();
    }
    
    public boolean isDispel() {
        return effect.getDispel();
    }
    
    public boolean isExpired() {
        return effect.hasDuration() && remaining == 0;
    }
    
    public void elapse() {
        if (effect.hasDuration()) {
            if (effect.getDuration() > 0) {
                remaining--;
            } else if (effect.getDuration() < 0) {
                remaining++;
            }
        }
    }
    
    public Result apply() {
        if (ready()) {
            return doApply();
        }
        
        // Not ready or countdown result
        return new Result(source.getActor(), "");
    }
    
    private boolean ready() {
        if (!effect.hasDuration() || effect.getDuration() >= 0) {
            return true;
        }
        return effect.getDuration() < 0 && remaining == 0;
    }
    
    protected abstract Result doApply();
    
    public static class RangedDamageEffect extends ActiveEffect {
        public RangedDamageEffect(Effect effect, ActorState source, ActorState target) {
            super(effect, source, target);
        }

        @Override
        protected Result doApply() {
            int value = target.damage(effect.getDamageType(), effect.getMagnitude());
            return new Result(target.getActor(), "-" + value);
        }
    }
    
    
    public static class RegenerateEffect extends ActiveEffect {
        public RegenerateEffect(Effect effect, ActorState source, ActorState target) {
            super(effect, source, target);
        }

        @Override
        protected Result doApply() {
            int value = target.heal(effect.getMagnitude());
            return new Result(target.getActor(), "+" + value);
        }
    }
}
