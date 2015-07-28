package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Ammunition;
import com.eldritch.invoken.actor.items.Consumable;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.proto.Items.Item.RangedWeaponType;
import com.eldritch.invoken.state.Inventory;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class AgentInventory extends Inventory {
    private static final float MAX_WEIGHT = 150f;
    private static final float BURDEN_RATIO = 1f;
    private static final float BURDEN_PENALTY = 5f;

    private final Set<Item> loot = new HashSet<>();
    private final AgentInfo info;

    // equipment
    private final Consumable[] consumables = new Consumable[2];
    private final Map<RangedWeaponType, Ammunition> ammunition = new HashMap<>();
    private Outfit outfit;
    private RangedWeaponState rangedWeapon = new RangedWeaponState(null, null);
    private MeleeWeapon meleeWeapon;

    // additional properties
    private float weight = 0;

    public AgentInventory(AgentInfo info) {
        super(new ArrayList<InventoryItem>());
        this.info = info;
    }

    public AgentInfo getAgentInfo() {
        return info;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || loot.isEmpty();
    }

    public void update(float delta) {
        if (hasRangedWeapon()) {
            ItemState state = getState(rangedWeapon.getWeapon().getId());
            if (state.getCooldown() > 0) {
                state.cooldown(delta);
            }
            rangedWeapon.update(delta);
        }
    }

    public float getWeight() {
        return weight;
    }

    public float getMaxWeight() {
        return MAX_WEIGHT;
    }

    public boolean canUseRangedWeapon() {
        // return hasRangedWeapon() && !isCooling(rangedWeapon);
        return hasRangedWeapon() && getClip() > 0 && getAmmunitionCount() > 0 && !isReloading()
                && !isCooling(rangedWeapon.getWeapon());
    }

    public void setCooldown(Item item, float cooldown) {
        getState(item.getId()).setCooldown(cooldown);
    }

    public boolean isCooling(Item item) {
        return getState(item.getId()).getCooldown() > 0;
    }

    public Consumable[] getConsumables() {
        return consumables;
    }

    public int getConsumableCount() {
        return consumables.length;
    }

    public boolean hasConsumable(int index) {
        return index < consumables.length && consumables[index] != null;
    }

    public Consumable getConsumable(int index) {
        return consumables[index];
    }

    public void setConsumable(int index, Consumable consumable) {
        consumables[index] = consumable;
    }

    public boolean canConsume(int index) {
        return canConsume(getConsumable(index));
    }

    public boolean canConsume(Consumable consumable) {
        return consumable != null && hasItem(consumable);
    }

    public boolean consume(int index) {
        return consume(consumables[index]);
    }

    public boolean consume(Consumable consumable) {
        if (canConsume(consumable)) {
            consumable.equipFrom(this);
            return true;
        }
        return false;
    }

    public Iterable<Ammunition> getAmmunition() {
        return ammunition.values();
    }

    public boolean hasAmmunition(RangedWeaponType type) {
        return ammunition.containsKey(type);
    }

    public Ammunition getAmmunition(RangedWeaponType type) {
        return ammunition.get(type);
    }

    public void setAmmunition(RangedWeaponType type, Ammunition ammo) {
        ammunition.put(type, ammo);
    }

    public void removeAmmunition(RangedWeaponType type) {
        ammunition.remove(type);
    }

    public boolean hasOutfit() {
        return outfit != null;
    }

    public Outfit getOutfit() {
        return outfit;
    }

    public void setOutfit(Outfit outfit) {
        if (this.outfit != null) {
            info.getAgent().addVelocityPenalty(-this.outfit.getWeightPenalty());
        }
        this.outfit = outfit;
        if (outfit != null) {
            info.getAgent().addVelocityPenalty(outfit.getWeightPenalty());
        }
    }

    public boolean hasRangedWeapon() {
        return rangedWeapon.getWeapon() != null;
    }

    public float getReloadFraction() {
        return rangedWeapon.getReloadFraction();
    }

    public boolean isReloading() {
        return rangedWeapon.reloading;
    }

    public void reloadWeapon() {
        rangedWeapon.reload();
    }

    public void useAmmunition(int x) {
        rangedWeapon.useAmmunition(x);
    }

    public int getAmmunitionCount() {
        return rangedWeapon.getAmmunitionCount();
    }

    public int getClip() {
        return rangedWeapon.getClip();
    }

    public RangedWeapon getRangedWeapon() {
        return rangedWeapon.getWeapon();
    }

    public void setRangedWeapon(RangedWeapon weapon) {
        Ammunition ammunition = null;
        if (weapon != null) {
            for (ItemState state : getItems()) {
                if (state.getItem() instanceof Ammunition) {
                    Ammunition current = (Ammunition) state.getItem();
                    if (current.getType() == weapon.getType()) {
                        ammunition = current;
                    }
                }
            }
        }

        rangedWeapon = new RangedWeaponState(weapon, ammunition);
    }

    public boolean hasMeleeWeapon() {
        return meleeWeapon != null;
    }

    public MeleeWeapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public void setMeleeWeapon(MeleeWeapon weapon) {
        this.meleeWeapon = weapon;
    }

    public void equipIfBetter(Item item) {
        if (info.getAgent().canEquip(item)) {
            item.equipIfBetter(this);
        }
    }

    public void equip(Item item) {
        if (info.getAgent().canEquip(item)) {
            item.equipFrom(this);
        }
    }

    public void unequip(Item item) {
        item.unequipFrom(this);
    }
    
    public void releaseItems() {
        for (Item item : getStatelessItems()) {
            item.releaseFrom(this);
        }
    }

    @Override
    protected void onAdd(Item item, int count) {
        item.addFrom(this);

        boolean wasBurdened = isBurdened();
        weight += item.getWeight() * count;
        updateBurden(wasBurdened);

        if (item.canLoot()) {
            loot.add(item);
        }
    }

    @Override
    protected void onRemove(Item item, int count) {
        boolean wasBurdened = isBurdened();
        weight -= item.getWeight() * count;
        updateBurden(wasBurdened);
    }

    @Override
    protected void handleRemove(Item item) {
        item.unequipFrom(this);
        super.handleRemove(item);
        if (item.canLoot()) {
            loot.remove(item);
        }
    }

    private void updateBurden(boolean wasBurdened) {
        boolean burdened = isBurdened();
        if (burdened != wasBurdened) {
            int sign = burdened ? 1 : -1;
            info.getAgent().addVelocityPenalty(sign * BURDEN_PENALTY);
        }
    }

    public boolean isBurdened() {
        return getWeight() / MAX_WEIGHT >= BURDEN_RATIO;
    }

    private class RangedWeaponState {
        private final RangedWeapon weapon;
        private Ammunition ammunition;
        private int clip;

        private boolean reloading = false;
        private float reloadElapsed = 0;

        public RangedWeaponState(RangedWeapon weapon, Ammunition ammunition) {
            this.weapon = weapon;
            this.ammunition = ammunition;
            if (weapon != null) {
                resetClip();
            }
        }

        public RangedWeapon getWeapon() {
            return weapon;
        }

        public void update(float delta) {
            if (reloading) {
                reloadElapsed += delta;
                if (getReloadFraction() >= 1) {
                    reloading = false;
                    reloadElapsed = 0;
                    InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.RANGED_WEAPON_RELOAD_END,
                            info.getAgent().getPosition());
                }
            }
        }

        public float getReloadFraction() {
            return reloadElapsed / (weapon.getCooldown() * 2);
        }

        public void reload() {
            if (weapon != null) {
                if (reloading == false) {
                    InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.RANGED_WEAPON_RELOAD_START,
                            info.getAgent().getPosition());
                }
                resetClip();
                reloading = true;
            } else {
                clip = 0;
                reloading = false;
            }
            reloadElapsed = 0;
        }

        private void resetClip() {
            clip = Math.min(weapon.getClipSize(), getAmmunitionCount());
        }

        public void useAmmunition(int x) {
            if (ammunition != null) {
                clip = Math.max(clip - x, 0);
                removeItem(ammunition, x);
            }
        }

        public int getAmmunitionCount() {
            if (ammunition == null || !hasItem(ammunition)) {
                return 0;
            }
            return getState(ammunition.getId()).getCount();
        }

        public int getClip() {
            return clip;
        }
    }
}
