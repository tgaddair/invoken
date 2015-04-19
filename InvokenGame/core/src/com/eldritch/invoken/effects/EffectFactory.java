package com.eldritch.invoken.effects;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Effects;

public class EffectFactory {
    private EffectFactory() {
        // singleton
    }

    public static EffectGenerator from(final Effects.Effect proto) {
        switch (proto.getType()) {
            case REGENERATE:
                return new EffectGenerator() {
                    @Override
                    public Effect generate(Agent target) {
                        return new Regenerating(target, proto.getMagnitude(), proto.getDuration());
                    }
                };
            default:
                throw new IllegalArgumentException("Unrecognized effect type: " + proto.getType());
        }
    }
    
    public interface EffectGenerator {
        Effect generate(Agent target);
    }
}
