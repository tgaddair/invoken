package com.eldritch.invoken.actor;

import com.eldritch.invoken.proto.Actors.ActorParams;

public enum Species {
    Automaton() {
        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.AUTOMATON;
        }
    },
    
    Beast() {
        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.BEAST;
        }
    },
    
    Hollow() {
        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.HOLLOW;
        }
    },
    
    Human() {
        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.HUMAN;
        }
    },
    
    Invoken() {
        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.INVOKEN;
        }
    },
    
    Undead() {
        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.UNDEAD;
        }
    };
    
    public abstract ActorParams.Species toProto();
    
    public static Species from(ActorParams.Species proto) {
        switch (proto) {
            case AUTOMATON:
                return Automaton;
            case BEAST:
                return Beast;
            case HOLLOW:
                return Hollow;
            case HUMAN:
                return Human;
            case INVOKEN:
                return Invoken;
            case UNDEAD:
                return Undead;
            default:
                throw new IllegalStateException("Unrecognized species: " + proto);
        }
    }
}
