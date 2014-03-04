package com.eldritch.scifirpg.game.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.model.actor.ActiveEffect;
import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.proto.Effects.DamageType;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.collect.ImmutableList;

public class EffectUtil {
    public static List<Actor> getTargets(Effect effect, Actor source, Actor selected,
            Collection<Actor> combatants) {
        switch (effect.getRange()) {
            case PLAYER: // Only affects the player
                return ImmutableList.of(source);
            case SELECTED: // Actively choose an Actor within an ActorEncounter
                return ImmutableList.of(selected);
            case ALL: // Everyone in an ActorEncounter, including the player
                return ImmutableList.<Actor> builder().addAll(combatants).build();
            case ALL_OTHER: // Everyone in an ActorEncounter, except the player
                Set<Actor> allOther = new LinkedHashSet<>(combatants);
                allOther.remove(source);
                return ImmutableList.<Actor> builder().addAll(allOther).build();
            case ALL_HOSTILE: // Everyone hostile to the player in an
                              // ActorEncounter
                // TODO return source.getEnemies();
            case ALL_ALLIED: // Everyone allied with the player in an
                             // ActorEncounter, including player
                // TODO return source.getAllies();
            case TARGETER: // Applies to counters, traps, and passive abilities
                           // that are triggered when someone targets player
            case SPREAD_ALL:
            case SPREAD_HOSTILE:
                return ImmutableList.of(selected);
            default:
                throw new IllegalArgumentException("Unrecognized Effect range: "
                        + effect.getRange());
        }
    }

    public static Result apply(Effect effect, Actor source, Actor target) {
        Result r = applyActive(effect, source, target);
        if (!effect.hasDuration() || effect.getDuration() != 0) {
            target.addActiveEffect(new ActiveEffect(effect, source, target));
        }
        return r;
    }

    public static Result applyActive(Effect effect, Actor source, Actor target) {
        switch (effect.getType()) {
        // Attack
            case DAMAGE_MELEE: // MAGNITUDE damage of DAMAGE_TYPE on TARGET for
                               // DURATION
            case DAMAGE_RANGED:
            case DAMAGE_HEAVY:
            case DAMAGE_COORDINATED:
            case DAMAGE_CORRUPTION: {
                int value = target.damage(effect.getDamageType(), effect.getMagnitude());
                return new Result(source, String.format("%d %s damage to %s", value, effect
                        .getDamageType().name().toLowerCase(), target.getName()));
            }
            case DRAIN: // Corruption: Transfer life to source, W/A
                int value = target.damage(DamageType.VIRAL, effect.getMagnitude());
                source.heal(value);
                return new Result(source, String.format("%d drained from %s to %s", value,
                        target.getName(), source.getName()));
            case SUPPRESS:

                // Combat modifiers
            case ACCURACY_MOD:
            case DEFENSE_MOD:
            case RESISTANCE_MOD:
            case PERCEPTION_MOD:
            case CRITICAL_MOD:

                // Deceive
            case SNEAK: // Avoid conflict and detection
            case PILFER: // Steal money or item from the TARGET
            case STALK: // Increase damage multiplier by MAGNITUDE without being
                        // detected
            case IMPERSONATE: // Pretend to belong to TARGET faction with rank
                              // MAGNITUDE
            case AUTHORIZE: // Does not improve reaction, but prevents trespass

                // Deceive - Espionage
            case DETECT_EQUIPMENT: // Discover what equipment TARGET has
                                   // currently
            case DETECT_AUGS: // Discover what augs TARGET has currently staged
            case DETECT_BUFFER: // Discover what augs TARGET has in their buffer
            case DETECT_TRAPS: // Discover what traps the TARGET has deployed

                // Deceive - Hacking
            case OPEN: // Break a lock
            case DISABLE: // Prevent use and effects of a given undeployed aug
                          // (useless w/o espionage)
            case SHUTDOWN: // Disable an active aug (useless on passive w/o
                           // espionage)

                // Execute - Analysis
            case SCAN: // learn stats for TARGET up to level MAGNITUDE, stats
                       // drop off with lower levels

                // Execute - Manipulation
            case PARALYZE: // TARGET unable to take action for DURATION
            case CONTROL: // TARGET up to level MAGNITUDE becomes ally for
                          // DURATION

                // Execute - Fabrication
            case CREATE_GOLEM: // MAGNITUDE is automaton level
            case CREATE_SWARM:
            case REANIMATE: // TARGET becomes a puppet ally on death for
                            // DURATION

                // Execute - Upgrade
            case REGENERATE: // MAGNITUDE change in health for DURATION on
                             // TARGET
            case BARRIER: // MAGNITUDE change in defense to DAMAGE_TYPE on
                          // TARGET for DURATION
            case MIRROR: // TARGET reflects up to MAGNITUDE damage of
                         // DAMAGE_TYPE for DURATION
            case ABSORB: // TARGET absorbs up to MAGNITUDE damage of DAMAGE_TYPE
                         // for DURATION

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
            case DEFEND: // Avoid a ranged attack
            case RESIST: // Prevent the targeted execution (includes
                         // corruption), W/A
            case REVEAL: // Discover deceptive activity, including Illusion, W/S
            case INTERRUPT: // Prevent negative duration effect

                // Dialogue - Influence
            case INFLUENCE: // Requires Influence Type
                return new Result(source, "");

            default:
                throw new IllegalArgumentException("Unrecognized Effect type: " + effect.getType());
        }
    }

    private EffectUtil() {
    }
}
