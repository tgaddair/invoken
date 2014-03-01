package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Effects.Effect;

public class ActiveEffect {
    private final Effect effect;
    
    public ActiveEffect(Effect effect) {
        this.effect = effect;
    }
}
