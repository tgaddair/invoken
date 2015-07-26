package com.eldritch.invoken.actor.ai.planning;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Locations.DesireProto;

public interface Desire {
    Npc getOwner();

    boolean isActive();

    float getActiveTime();

    void start();

    void update(float delta);

    void stop();

    boolean act();

    float getValue(); // [0, 1]

    float getPriority();
    
    DesireProto toProto();

    public abstract static class AbstractDesire implements Desire {
        protected final Npc owner;
        private boolean active = false;
        private float elapsed = 0;

        public AbstractDesire(Npc owner) {
            this.owner = owner;
        }

        @Override
        public Npc getOwner() {
            return owner;
        }

        @Override
        public float getPriority() {
            return 1;
        }

        @Override
        public void start() {
            active = true;
            onStart();
        }

        @Override
        public void stop() {
            active = false;
            elapsed = 0;
            onStop();
        }

        @Override
        public void update(float delta) {
            if (active) {
                elapsed += delta;
                activeUpdate(delta);
            } else {
                passiveUpdate(delta);
            }
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public float getActiveTime() {
            return elapsed;
        }

        protected void onStart() {
        }

        protected void activeUpdate(float delta) {
        }
        
        protected void passiveUpdate(float delta) {
        }

        protected void onStop() {
        }
        
        @Override
        public DesireProto toProto() {
            return DesireProto.valueOf(getClass().getSimpleName());
        }
    }

    public static class DesireFactory {
        public static Desire fromProto(DesireProto proto, Npc npc) {
            String className = Desire.class.getPackage().getName() + "." + proto.name();
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getConstructor(Npc.class);
                Desire instance = (Desire) ctor.newInstance(npc);
                return instance;
            } catch (InvocationTargetException e) {
                InvokenGame.error("Unable to instantiate " + className, e);
            } catch (IllegalAccessException e) {
                InvokenGame.error("Unable to access " + className, e);
            } catch (ClassNotFoundException e) {
                InvokenGame.error("Unable to find class " + className, e);
            } catch (IllegalArgumentException e) {
                InvokenGame.error("Bad argument for " + className, e);
            } catch (NoSuchMethodException e) {
                InvokenGame.error("No method for " + className, e);
            } catch (SecurityException e) {
                InvokenGame.error("Security violation for " + className, e);
            } catch (InstantiationException e) {
                InvokenGame.error("Unable to instantiate " + className, e);
            }
            return null;
        }
    }
}
