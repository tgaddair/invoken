package com.eldritch.invoken.activators;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Conversable;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.ConversationHandler.DialogueVerifier;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Actors.Terminal;
import com.eldritch.invoken.util.OutcomeHandler;
import com.eldritch.invoken.util.PrerequisiteVerifier;

public class InfoTerminal extends ClickActivator implements Conversable {
    private final String id;
    private final ConversationHandler dialogue;
    private final Vector2 focus;

    public InfoTerminal(NaturalVector2 position, Terminal proto) {
        super(position, 2, 2);
        this.id = proto.getId();
        dialogue = new ConversationHandler(proto.getDialogueList(), new TerminalDialogueVerifier(),
                new TerminalOutcomeHandler());
        this.focus = getPosition().cpy().add(1, 1);
    }

    @Override
    public void activate(Agent agent, Level level) {
        agent.beginDialogue(this);
    }

    @Override
    public void postRegister(Level level) {
    }

    @Override
    public ConversationHandler getDialogueHandler() {
        return dialogue;
    }

    @Override
    public boolean canConverse() {
        return true;
    }

    @Override
    public void endDialogue() {
    }

    @Override
    public Vector2 getRenderPosition() {
        return focus;
    }

    public class TerminalDialogueVerifier extends PrerequisiteVerifier implements DialogueVerifier {
        @Override
        public boolean isValid(Response r, Agent interactor) {
            if (interactor.hasHeardDialogue(getParentId(r))) {
                // already heard this dialogue
                return false;
            }

            return verify(r.getPrereqList(), interactor);
        }

        @Override
        public boolean isValid(Choice c, Agent interactor) {
            return verify(c.getPrereqList(), interactor);
        }

        @Override
        protected Agent getSource() {
            return null;
        }
    }

    public class TerminalOutcomeHandler extends OutcomeHandler {
        @Override
        protected Agent getSource() {
            return null;
        }

        @Override
        protected String getId(Response response) {
            return getParentId(response);
        }
    }

    private String getParentId(Response response) {
        return String.format("%s:%s", id, response.getId());
    }

    @Override
    public boolean canInteract() {
        return true;
    }

    @Override
    public void endInteraction() {
    }
}
