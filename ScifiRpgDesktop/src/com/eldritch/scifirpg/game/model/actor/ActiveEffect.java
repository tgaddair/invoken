package com.eldritch.scifirpg.game.model.actor;

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
    
    public void apply() {
        if (ready()) {
            //EffectUtil.apply(effect, source, target);
        }
    }
    
    private boolean ready() {
        if (!effect.hasDuration() || effect.getDuration() >= 0) {
            return true;
        }
        return effect.getDuration() < 0 && remaining == 0;
    }
    
    protected abstract void doApply();
    
    public static class RangedDamageEffect extends ActiveEffect {
        public RangedDamageEffect(Effect effect, ActorState source, ActorState target) {
            super(effect, source, target);
        }

        @Override
        protected void doApply() {
            target.damage(effect.getDamageType(), effect.getMagnitude());
        }
    }
    
    
    public static class RegenerateEffect extends ActiveEffect {
        public RegenerateEffect(Effect effect, ActorState source, ActorState target) {
            super(effect, source, target);
        }

        @Override
        protected void doApply() {
            target.heal(effect.getMagnitude());
        }
    }
}
