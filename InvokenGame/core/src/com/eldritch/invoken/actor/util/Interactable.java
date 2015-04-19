package com.eldritch.invoken.actor.util;

public interface Interactable extends Locatable {
    boolean canInteract();
    
    void endInteraction();
}
