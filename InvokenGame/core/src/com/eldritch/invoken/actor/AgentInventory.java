package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private final AgentInfo info;

    // equipment
    private final Consumable[] consumables = new Consumable[2];
    private final Map<RangedWeaponType, Ammunition> ammunition = new HashMap<>();
    private Outfit outfit;
    private RangedWeaponState rangedWeapon = new RangedWeaponState(null, null);
    private MeleeWeapon meleeWeapon;

    public AgentInventory(AgentInfo info) {
        super(new ArrayList<InventoryItem>());
        this.info = info;
    }

    public AgentInfo getAgentInfo() {
        return info;
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

    public boolean canUseRangedWeapon() {
        // return hasRangedWeapon() && !isCooling(rangedWeapon);
        return hasRangedWeapon() && getClip() > 0 && getAmmunitionCount() > 0 && !isReloading();
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
            info.getAgent().addVelocityPenalty(-this.outfit.getWeight());
        }
        this.outfit = outfit;
        if (outfit != null) {
            info.getAgent().addVelocityPenalty(outfit.getWeight());
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
        item.equipIfBetter(this);
    }

    public void equip(Item item) {
        item.equipFrom(this);
    }

    public void unequip(Item item) {
        item.unequipFrom(this);
    }
    
    @Override
    protected void handleAdd(Item item, int count) {
        item.addFrom(this);
    }

    @Override
    protected void handleRemove(Item item) {
        item.unequipFrom(this);
        super.handleRemove(item);
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
                getState(ammunition.getId()).remove(x);
            }
        }

        public int getAmmunitionCount() {
            if (ammunition == null) {
                return 0;
            }
            return getState(ammunition.getId()).getCount();
        }

        public int getClip() {
            return clip;
        }
    }
}
