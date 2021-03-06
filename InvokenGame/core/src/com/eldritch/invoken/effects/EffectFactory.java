package com.eldritch.invoken.effects;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Effects;

public class EffectFactory {
    private EffectFactory() {
        // singleton
    }
    
    public static List<EffectGenerator> from(Item item, List<Effects.Effect> protos) {
        List<EffectGenerator> generators = new ArrayList<>();
        for (Effects.Effect proto : protos) {
            generators.add(from(item, proto));
        }
        return generators;
    }

    public static EffectGenerator from(final Item item, final Effects.Effect proto) {
        switch (proto.getType()) {
            case REGENERATE:
                return new EffectGenerator() {
                    @Override
                    public Effect generate(Agent target) {
                        return new Regenerating(target, proto.getMagnitude(), proto.getDuration());
                    }
                };
            case ENERGIZE:
                return new EffectGenerator() {
                    @Override
                    public Effect generate(Agent target) {
                        return new Energizing(target, proto.getMagnitude(), proto.getDuration());
                    }
                };
            case IMPERSONATE:
                return new EffectGenerator() {
                    @Override
                    public Effect generate(Agent target) {
                        Faction faction = target.getLocation().getFaction(proto.getTarget());
                        return new Disguised(target, item, faction, proto.getMagnitude());
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
