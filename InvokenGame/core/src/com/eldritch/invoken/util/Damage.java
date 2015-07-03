package com.eldritch.invoken.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector2;
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
    private float baseScale = 1.0f;

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
        float scale = getBaseScale(defender);
        if (attacker != defender && attacker.isAlly(defender)) {
            // scale down friendly fire
            scale *= 0.1f;
        }
        if (!reified.containsKey(defender)) {
            ReifiedDamage damage = new ReifiedDamage();
            float attackMod = attacker.getInfo().getAttackModifier();
            for (DamageMod mod : components) {
                // handle each damage type separately
                float atk = mod.getMagnitude() * attackMod;
                float def = defender.getInfo().getDefense(mod.getDamage());
                float s = defender.getInfo().getDamageScale(mod.getDamage());
                float magnitude = s * getDamage(atk, def);
//                System.out.println(String.format("atk %.2f def %.2f scale %.2f", atk, def, s));
                damage.add(mod.getDamage(), magnitude * scale);
            }
            
            System.out.println(String.format("%s -> %s = %.2f", attacker, defender, damage.total));
            reified.put(defender, damage);
        }
        
        // lazily resolve the magnitude, but only do so once
        return reified.get(defender);
    }
    
    public void setBaseScale(float scale) {
        this.baseScale = scale;
    }
    
    protected float getBaseScale(Agent defender) {
        return baseScale;
    }
    
    private float getDamage(float atk, float def) {
        double damage = 0;
        if (atk < def) {
            // Damage = 0.4*(Atk^3/ Def^2) - 0.09*(Atk^2/ Def)+0.1*Atk
            damage = 0.4f * (Math.pow(atk, 3f) / Math.pow(def, 2f)) 
                    - 0.09 * (Math.pow(atk, 2f) / def) + 0.1f * atk;
        } else {
            // Damage = Atk - 0.79* Def*e^(-0.27* Def/Atk)
            damage = atk - 0.79f * def * Math.pow(Math.E, -0.27f * def / atk);
        }
        return (float) damage;
    }

    public static Damage from(Agent attacker, RangedWeapon weapon) {
        return new Damage(attacker, weapon.getDamageList());
    }
    
    public static Damage from(Agent attacker, RangedWeapon weapon, Vector2 origin, float scale) {
        return new DistanceScaledDamage(attacker, weapon.getDamageList(), origin, scale);
    }

    public static Damage from(Agent attacker, DamageType type, int magnitude) {
        return new Damage(attacker, ImmutableList.of(DamageMod.newBuilder().setDamage(type)
                .setMagnitude(magnitude).build()));
    }

    public static Damage from(Agent attacker) {
        return new Damage(attacker, ImmutableList.<DamageMod> of());
    }
    
    private static class DistanceScaledDamage extends Damage {
        private static final float MAX_DST2 = 50f;
        
        private final Vector2 origin = new Vector2();
        private final float scale;
        
        public DistanceScaledDamage(Agent attacker, List<DamageMod> components, Vector2 origin, float scale) {
            super(attacker, components);
            this.origin.set(origin);
            this.scale = scale;
        }
        
        @Override
        protected float getBaseScale(Agent defender) {
            float dst2 = defender.getPosition().dst2(origin);
            return scale * Math.max((MAX_DST2 - dst2) / MAX_DST2, 0.25f);
        }
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
