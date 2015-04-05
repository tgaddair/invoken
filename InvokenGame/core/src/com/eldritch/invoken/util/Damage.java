package com.eldritch.invoken.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.proto.Items.Item.DamageMod;
import com.google.common.collect.ImmutableList;

/**
 * Lazy calculator of damage.
 */
public class Damage {
    private final Map<Agent, ReifiedDamage> reified = new HashMap<>();
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
    
    public float apply(Agent defender, float delta) {
        ReifiedDamage damage = getDamage(defender);
        for (Entry<DamageType, Float> entry : damage.magnitudes.entrySet()) {
            defender.getInfo().addStatus(entry.getKey(), entry.getValue());
        }
        return damage.total * delta;
    }
    
    public float get(Agent defender) {
        return getDamage(defender).total;
    }
    
    private ReifiedDamage getDamage(Agent defender) {
        if (!reified.containsKey(defender)) {
            ReifiedDamage damage = new ReifiedDamage();
            float attackMod = attacker.getInfo().getAttackModifier();
            for (DamageMod mod : components) {
                // handle each damage type separately
                float baseDamage = mod.getMagnitude();
                float dr = defender.getInfo().getDamageReduction(mod.getDamage(), baseDamage);
                float s = defender.getInfo().getDamageScale(mod.getDamage());
                float magnitude = s * ((baseDamage * attackMod) / dr);
                damage.add(mod.getDamage(), magnitude);
            }
            
            System.out.println(String.format("%s -> %s = %.2f", attacker, defender, damage.total));
            reified.put(defender, damage);
        }
        
        // lazily resolve the magnitude, but only do so once
        return reified.get(defender);
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
    
    private static class ReifiedDamage {
        private final Map<DamageType, Float> magnitudes = new EnumMap<>(DamageType.class);
        private float total = 0;
        
        public void add(DamageType damage, float magnitude) {
            magnitudes.put(damage, magnitude);
            total += magnitude;
        }
    }
}
