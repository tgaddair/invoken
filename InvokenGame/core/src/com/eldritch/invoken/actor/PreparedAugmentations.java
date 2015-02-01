package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;

public class PreparedAugmentations {
	private final List<Augmentation> augs = new ArrayList<Augmentation>();
	private final Agent owner;
	private final Set<Augmentation> activeSelfAugmentations = new HashSet<Augmentation>();
	private final Map<Integer, Augmentation> activeAugmentations = new HashMap<Integer, Augmentation>();
	private final Set<Augmentation> prepared = new HashSet<Augmentation>();
	
	public PreparedAugmentations(Agent owner) {
		this.owner = owner;
	}
	
	public Iterable<Augmentation> getAugmentations() {
	    return augs;
	}
	
	public Augmentation getAugmentation(int index) {
	    return augs.get(index);
	}
	
	public boolean isActive(Augmentation aug) {
		for (Augmentation active : activeAugmentations.values()) {
			if (active == aug) {
				return true;
			}
		}
	    return activeSelfAugmentations.contains(aug);
	}
	
	public Set<Augmentation> getActiveSelfAugmentations() {
	    return activeSelfAugmentations;
	}
	
	public void addAugmentation(Augmentation aug) {
		augs.add(aug);
	}
	
	public boolean hasActiveAugmentation() {
	    return !activeAugmentations.isEmpty();
	}
	
	public boolean hasActiveAugmentation(int slot) {
	    return activeAugmentations.containsKey(slot);
	}
	
	public void toggleActiveAugmentation(int index) {
	    if (index < augs.size()) {
	        toggleActiveAugmentation(augs.get(index), 0);
	    }
	}
	
	public void removeSelfAugmentation(Augmentation aug) {
		activeSelfAugmentations.remove(aug);
	}
	
	public void toggleActiveAugmentation(Augmentation aug, int slot) {
		if (aug.castsOnSelf()) {
	        boolean used = use(aug);
	        if (activeSelfAugmentations.contains(aug)) {
	            activeSelfAugmentations.remove(aug);
	        } else if (used) {
	            activeSelfAugmentations.add(aug);
	        }
	    } else {
	    	Iterator<Entry<Integer, Augmentation>> it = activeAugmentations.entrySet().iterator();
	    	while (it.hasNext()) {
	    		Entry<Integer, Augmentation> active = it.next();
				if (active.getValue() == aug && active.getKey() != slot) {
					// the aug is already active in a different slot, so remove it
					it.remove();
				}
			}
	        if (activeAugmentations.get(slot) == aug) {
	        	// already active in this slot
	            unprepare(aug);
	        	activeAugmentations.remove(slot);
	        } else {
	        	// activate the aug
	            prepare(aug);
	        	activeAugmentations.put(slot, aug);
	        }
	    }
	}
	
	public void useActiveAugmentation() {
	    useActiveAugmentation(0, false);
	}
	
	public void useActiveAugmentation(int slot, boolean queued) {
		if (activeAugmentations.containsKey(slot)) {
	        use(activeAugmentations.get(slot), queued);
	    }
	}
	
	public void useActiveAugmentation(Vector2 position, int slot, boolean queued) {
		if (activeAugmentations.containsKey(slot)) {
            use(activeAugmentations.get(slot), position, queued);
        }
	}
	
    public boolean use(int index) {
        return use(index, false);
    }
    
    public boolean use(int index, boolean queued) {
        if (index < augs.size()) {
            return use(augs.get(index), queued);
        }
        return false;
    }
    
    public boolean use(Augmentation aug) {
        return use(aug, false);
    }
    
    public boolean use(Augmentation aug, boolean queued) {
        if (queued || owner.canAddAction()) {
            return aug.invoke(owner, owner.getTarget());
        }
        return false;
    }
    
    public boolean use(Augmentation aug, Vector2 position, boolean queued) {
        if (queued || owner.canAddAction()) {
            return aug.invoke(owner, position);
        }
        return false;
    }
    
    private void prepare(Augmentation aug) {
        if (!prepared.contains(aug)) {
            aug.prepare(owner);
            prepared.add(aug);
        }
    }
    
    private void unprepare(Augmentation aug) {
        if (prepared.contains(aug)) {
            aug.unprepare(owner);
            prepared.remove(aug);
        }
    }
}
