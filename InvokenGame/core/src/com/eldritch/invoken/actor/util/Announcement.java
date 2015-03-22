package com.eldritch.invoken.actor.util;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public interface Announcement {
    String getText();

    void onFinish();

    public static class BasicAnnouncement implements Announcement {
        private final String text;

        public BasicAnnouncement(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void onFinish() {
        }
    }

    public static class BanterAnnouncement implements Announcement {
        private final Agent owner;
        private final String text;
        private BanterAnnouncement next = null;

        public BanterAnnouncement(Agent owner, String text) {
            this.owner = owner;
            this.text = text;
        }
        
        public void setNext(BanterAnnouncement next) {
            this.next = next;
        }

        protected Agent getOwner() {
            return owner;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void onFinish() {
            if (next != null) {
                next.owner.announce(next);
            } else {
                owner.endDialogue();
            }
        }
    }

    public static class ResponseAnnouncement extends BanterAnnouncement {
        private final Agent listener;
        private final Response response;

        public ResponseAnnouncement(Agent owner, Agent listener, Response response) {
            super(owner, response.getText());
            this.listener = listener;
            this.response = response;
        }

        @Override
        public void onFinish() {
            getOwner().getDialogueHandler().handle(response, listener);
            super.onFinish();
        }
    }
}
