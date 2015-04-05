package com.eldritch.invoken.actor;

import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Effects.DamageType;

public enum Species {
    Automaton() {
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                case PHYSICAL:
                    // very resistant
                    return 0.5f;
                case THERMAL:
                case TOXIC:
                    // weak
                    return 1.5f;
                case RADIOACTIVE:
                case VIRAL:
                    // immune
                    return 0;
                default:
                    return 1;
                
            }
        }

        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.AUTOMATON;
        }
    },
    
    Beast() {
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                case PHYSICAL:
                case RADIOACTIVE:
                case VIRAL:
                    // weak
                    return 1.25f;
                case THERMAL:
                case TOXIC:
                    // resistant
                    return 0.5f;
                default:
                    return 1;
            }
        }

        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.BEAST;
        }
    },
    
    Hollow() {
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                case VIRAL:
                    // resistant
                    return 0.5f;
                default:
                    return 1;
            }
        }

        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.HOLLOW;
        }
    },
    
    Human() {
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                default:
                    return 1;
            }
        }

        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.HUMAN;
        }
    },
    
    Invoken() {
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                case VIRAL:
                    // immune
                    return 0;
                default:
                    // very resistant to everything else
                    return 0.1f;
            }
        }

        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.INVOKEN;
        }
    },
    
    Undead() {
        @Override
        public float getDamageScale(DamageType damage) {
            switch (damage) {
                default:
                    return 1;
            }
        }

        @Override
        public ActorParams.Species toProto() {
            return ActorParams.Species.UNDEAD;
        }
    };
    
    public abstract float getDamageScale(DamageType damage);
    
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
