package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.aug.Augmentation;

public class PreparedAugmentations {
	private final List<Augmentation> augs = new ArrayList<Augmentation>();
	private final Agent owner;
	private Augmentation activeAugmentation;
	
	public PreparedAugmentations(Agent owner) {
		this.owner = owner;
	}
	
	public void addAugmentation(Augmentation aug) {
		augs.add(aug);
	}
	
	public boolean hasActiveAugmentation() {
	    return activeAugmentation != null;
	}
	
	public void toggleActiveAugmentation(int index) {
	    if (index < augs.size()) {
	        if (augs.get(index) != activeAugmentation) {
	            setActiveAugmentation(augs.get(index));
	        } else {
	            clearActiveAugmentation();
	        }
	    }
	}
	
	public void clearActiveAugmentation() {
	    setActiveAugmentation(null);
	}
	
	public void setActiveAugmentation(Augmentation aug) {
	    activeAugmentation = aug;
	}
	
	public void useActiveAugmentation() {
	    if (activeAugmentation != null) {
	        use(activeAugmentation);
	    }
	}
	
    public void use(int index) {
        if (index < augs.size()) {
            use(augs.get(index));
        }
    }
    
    public void use(Augmentation aug) {
        if (owner.canAddAction()) {
            aug.invoke(owner, owner.getTarget());
        }
    }
}
