package com.eldritch.invoken.actor.util;

import com.eldritch.invoken.state.Inventory;

public interface Lootable extends Interactable {
    Inventory getInventory();
}
