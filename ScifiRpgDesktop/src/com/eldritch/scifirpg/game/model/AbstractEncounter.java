package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.Type;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;

public abstract class AbstractEncounter implements Comparable<AbstractEncounter> {
	private final Encounter data;

	public AbstractEncounter(Encounter data) {
		this.data = data;
	}
	
	public String getId() {
		return data.getId();
	}
	
	public String getTitle() {
		return data.getTitle();
	}
	
	public Type getType() {
		return data.getType();
	}
	
	public double getWeight() {
		return data.getWeight();
	}
	
	public boolean isUnique() {
		return data.getUnique();
	}
	
	public boolean satisfiesPrerequisites() {
	    // TODO
	    return true;
	}
	
	public List<Prerequisite> getPrerequisites() {
		return data.getPrereqList();
	}
	
	public String getSuccessorId() {
		return data.getSuccessorId();
	}
	
	public boolean canReturn() {
		return data.getReturn();
	}
	
	@Override
    public int compareTo(AbstractEncounter other) {
	    return Double.compare(this.getWeight(), other.getWeight());
    }
	
	public static AbstractEncounter getEncounter(Encounter encounter) {
		switch (encounter.getType()) {
		    case STATIC:
		        return new StaticEncounter(encounter);
		    case DECISION:
		        return new DecisionEncounter(encounter);
		    case ACTOR:
		        return new ActorEncounter(encounter);
		    case REGION:
		        return new RegionEncounter(encounter);
	        default:
	            throw new IllegalArgumentException(
	                    "Unrecognized Encounter type " + encounter.getType());
		        
		}
	}
}
