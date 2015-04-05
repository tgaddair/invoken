package com.eldritch.invoken.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.google.common.collect.ImmutableList;

/**
 * Lazy calculator of damage.
 */
public class Damage {
    private final Map<Agent, Float> magnitudes = new HashMap<>();
    private final Agent attacker;
    private final List<DamageMod> components;

    public Damage(Agent attacker, List<DamageMod> components) {
        this.attacker = attacker;
        this.components = components;
    }
    
    public Agent getSource() {
        return attacker;
    }

    public float getMagnitude() {
        float magnitude = 0;
        for (DamageMod mod : components) {
            magnitude += mod.getMagnitude();
        }
        return magnitude;
    }
    
    public float get(Agent defender, float delta) {
        return get(defender) * delta;
    }

    public float get(Agent defender) {
        if (!magnitudes.containsKey(defender)) {
            float total = 0;
            float attackMod = attacker.getInfo().getAttackModifier();
            for (DamageMod mod : components) {
                // handle each damage type separately
                float baseDamage = mod.getMagnitude();
                float dr = defender.getInfo().getDamageReduction(mod.getDamage(), baseDamage);
                float s = defender.getInfo().getDamageScale(mod.getDamage());
                total += s * ((baseDamage * attackMod) / dr);
            }
            System.out.println(String.format("%s -> %s = %.2f", attacker, defender, total));
            magnitudes.put(defender, total);
        }
        
        // lazily resolve the magnitude, but only do so once
        return magnitudes.get(defender);
    }

    public static Damage from(Agent attacker, RangedWeapon weapon) {
        return new Damage(attacker, weapon.getDamageList());
    }

    public static Damage from(Agent attacker, DamageType type, int magnitude) {
        return new Damage(attacker, ImmutableList.of(DamageMod.newBuilder().setDamage(type)
                .setMagnitude(magnitude).build()));
    }

    public static Damage from(Agent attacker) {
        return new Damage(attacker, ImmutableList.<DamageMod> of());
    }
}
