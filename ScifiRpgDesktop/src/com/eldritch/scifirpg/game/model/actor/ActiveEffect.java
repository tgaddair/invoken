package com.eldritch.scifirpg.game.model.actor;

import com.eldritch.scifirpg.game.util.EffectUtil;
import com.eldritch.scifirpg.proto.Effects.Effect;

public class ActiveEffect {
    private final Effect effect;
    private final Actor source;
    private final Actor target;
    private int remaining;
    
    public ActiveEffect(Effect effect, Actor source, Actor target) {
        this.effect = effect;
        this.source = source;
        this.target = target;
        this.remaining = effect.hasDuration() ? effect.getDuration() : 0;
    }
    
    public boolean isExpired() {
        return effect.hasDuration() && remaining == 0;
    }
    
    public boolean countdownComplete() {
        if (!effect.hasDuration()) {
            return false;
        }
        return effect.getDuration() < 0 && remaining == 0;
    }
    
    public void apply() {
        elapse();
        if (!isExpired() || countdownComplete()) {
            //EffectUtil.apply(effect, source, target);
        }
    }
    
    private void elapse() {
        if (effect.hasDuration()) {
            if (effect.getDuration() > 0) {
                remaining--;
            } else if (effect.getDuration() < 0) {
                remaining++;
            }
        }
    }
}
