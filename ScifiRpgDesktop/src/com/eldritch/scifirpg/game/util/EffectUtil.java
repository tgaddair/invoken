package com.eldritch.scifirpg.game.util;

import com.eldritch.scifirpg.game.model.Actor;
import com.eldritch.scifirpg.proto.Effects.DamageType;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.base.Optional;

public class EffectUtil {
    public static Result apply(Effect effect, Optional<Actor> source, Optional<Actor> target) {
        switch (effect.getType()) {
            // Attack
            case DAMAGE_MELEE: // MAGNITUDE damage of DAMAGE_TYPE on TARGET for DURATION
            case DAMAGE_RANGED:
            case DAMAGE_HEAVY:
            case DAMAGE_COORDINATED:
            case DAMAGE_CORRUPTION: {
                int value = target.get().damage(effect.getDamageType(), effect.getMagnitude());
                return new Result(String.format(
                        "%d %s damage to %s",
                        value,
                        effect.getDamageType().name().toLowerCase(),
                        target.get().getName()));
            }
            case DRAIN: // Corruption: Transfer life to source, W/A
                int value = target.get().damage(DamageType.VIRAL, effect.getMagnitude());
                source.get().heal(value);
                return new Result(String.format(
                        "%d drained from %s to %s",
                        value,
                        target.get().getName(),
                        source.get().getName()));

            // Deceive
            case FLEE: // Avoid conflict and detection
            case ROB: // Steal money or item from the TARGET
            case STALK: // Increase damage multiplier by MAGNITUDE without being detected
            case IMPERSONATE: // Pretend to belong to TARGET faction with rank MAGNITUDE
            case AUTHORIZE: // Does not improve reaction, but prevents trespass

            // Deceive - Espionage
            case DETECT_EQUIPMENT: // Discover what equipment TARGET has currently
            case DETECT_AUGS: // Discover what augs TARGET has currently staged
            case DETECT_BUFFER: // Discover what augs TARGET has in their buffer
            case DETECT_TRAPS:  // Discover what traps the TARGET has deployed

            // Deceive - Hacking
            case OPEN: // Break a lock
            case DISABLE: // Prevent use and effects of a given undeployed aug (useless w/o espionage)
            case SHUTDOWN: // Disable an active aug (useless on passive w/o espionage)

            // Execute - Analysis
            case SCAN: // learn stats for TARGET up to level MAGNITUDE, stats drop off with lower levels

            // Execute - Manipulation
            case PARALYZE: // TARGET unable to take action for DURATION
            case CONTROL: // TARGET up to level MAGNITUDE becomes ally for DURATION

            // Execute - Fabrication
            case CREATE_GOLEM: // MAGNITUDE is automaton level
            case CREATE_SWARM:
            case REANIMATE: // TARGET becomes a puppet ally on death for DURATION

            // Execute - Upgrade
            case REGENERATE: // MAGNITUDE change in health for DURATION on TARGET
            case BARRIER: // MAGNITUDE change in defense to DAMAGE_TYPE on TARGET for DURATION
            case MIRROR: // TARGET reflects up to MAGNITUDE damage of DAMAGE_TYPE for DURATION
            case ABSORB: // TARGET absorbs up to MAGNITUDE damage of DAMAGE_TYPE for DURATION

            // Execute - Illusion
            case CLOAK: // TARGET unable to be targeted directly for DURATION
            case MUFFLE: // TARGET gets drop bonus of MAGNITUDE

            // Execute - Empathy
            case DETECT_TRAITS:
            case CHARM:
            case CALM:
            case FRENZY:

            // Counter
            case PARRY: // Deflect a melee attack
            case DODGE: // Avoid a ranged attack
            case RESIST: // Prevent the targeted execution (includes corruption), W/A
            case REVEAL: // Discover deceptive activity, including Illusion, W/S
            case INTERRUPT: // Prevent negative duration effect

            // Dialogue - Influence
            case INFLUENCE: // Requires Influence Type
                return new Result("");
                
            default:
                throw new IllegalArgumentException(
                        "Unrecognized Effect type: " + effect.getType());
        }
    }
    
    public static class Result {
        private final String message;
        
        public Result(String message) {
            this.message = message;
        }
        
        @Override
        public String toString() {
            return message;
        }
    }
    
    private EffectUtil() {}
}
