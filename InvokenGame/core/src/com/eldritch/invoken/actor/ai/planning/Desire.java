package com.eldritch.invoken.actor.ai.planning;

import com.eldritch.invoken.actor.type.Npc;

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
    }
}
