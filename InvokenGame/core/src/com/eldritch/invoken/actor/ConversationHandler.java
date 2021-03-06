package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;
import com.eldritch.invoken.util.OutcomeHandler;
import com.eldritch.invoken.util.OutcomeHandler.DefaultOutcomeHandler;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ConversationHandler {
    private final Map<String, Response> responses = Maps.newHashMap();
    private final Map<String, Choice> choices = Maps.newHashMap();
    private final Map<DialogueTree, List<Response>> greetings = Maps.newHashMap();
    private final Map<String, List<Response>> targeted = Maps.newHashMap();
    private final Set<Response> forced = Sets.newHashSet();

    private final List<DialogueTree> trees;
    private final DialogueVerifier verifier;
    private final OutcomeHandler outcomes;
    private final boolean canSpeak;

    public ConversationHandler(List<DialogueTree> trees, DialogueVerifier verifier,
            OutcomeHandler outcomes) {
        this.trees = trees;
        this.verifier = verifier;
        this.outcomes = outcomes;

        boolean hasGreeting = false;
        for (DialogueTree tree : trees) {
            greetings.put(tree, new ArrayList<Response>());
            for (Response response : tree.getDialogueList()) {
                responses.put(response.getId(), response);
                if (response.getGreeting()) {
                    greetings.get(tree).add(response);
                    hasGreeting = true;

                    // check for targeted
                    String target = getTarget(response);
                    if (target != null) {
                        if (!targeted.containsKey(target)) {
                            targeted.put(target, new ArrayList<Response>());
                        }
                        targeted.get(target).add(response);
                    }
                }
                if (response.getForced()) {
                    forced.add(response);
                }
            }
            for (Choice choice : tree.getChoiceList()) {
                choices.put(choice.getId(), choice);
            }
        }

        canSpeak = hasGreeting;
    }

    public boolean canSpeak() {
        return canSpeak;
    }

    public void handle(Response response, Agent interactor) {
        outcomes.handle(response, interactor);
    }

    // relative to the owner of the tree
    public List<Choice> getChoicesFor(Response response, Agent interactor) {
        List<Choice> validChoices = new ArrayList<Choice>();
        for (String id : response.getChoiceIdList()) {
            Choice choice = choices.get(id);
            if (verifier.isValid(choice, interactor)) {
                validChoices.add(choice);
            }
        }
        return validChoices;
    }

    public Choice getChoiceFor(Response response, Agent interactor) {
        for (String id : response.getChoiceIdList()) {
            Choice choice = choices.get(id);
            if (verifier.isValid(choice, interactor)) {
                return choice;
            }
        }
        return null;
    }

    public Response getResponseFor(Choice choice, Agent interactor) {
        for (String id : choice.getSuccessorIdList()) {
            Response response = responses.get(id);
            if (verifier.isValid(response, interactor)) {
                return response;
            }
        }
        return null;
    }

    public boolean hasForcedGreeting() {
        return !forced.isEmpty();
    }

    public Response getForcedGreeting(Agent interactor) {
        for (Response r : forced) {
            if (verifier.isValid(r, interactor)) {
                return r;
            }
        }
        return null;
    }

    public Response getTargetedGreeting(Agent interactor) {
        if (targeted.containsKey(interactor.getInfo().getId())) {
            for (Response r : targeted.get(interactor.getInfo().getId())) {
                if (verifier.isValid(r, interactor)) {
                    return r;
                }
            }
        }
        return null;
    }

    public boolean hasGreeting() {
        return !greetings.isEmpty();
    }

    public Response getGreeting(Agent interactor) {
        for (DialogueTree tree : trees) {
            Response greeting = getGreetingFor(tree, interactor);
            if (greeting != null) {
                return greeting;
            }
        }
        return null;
    }

    private Response getGreetingFor(DialogueTree tree, Agent interactor) {
        if (greetings.containsKey(tree)) {
            for (Response r : greetings.get(tree)) {
                if (r.getGreeting() && verifier.isValid(r, interactor)) {
                    return r;
                }
            }
        }
        return null;
    }

    private static String getTarget(Response r) {
        for (Prerequisite p : r.getPrereqList()) {
            if (p.getType() == Prerequisite.Type.INTERACTOR) {
                return p.getTarget();
            }
        }
        return null;
    }

    public static interface DialogueVerifier {
        boolean isValid(Response r, Agent interactor);

        boolean isValid(Choice c, Agent interactor);
    }

    public static class DefaultDialogueVerifier implements DialogueVerifier {
        @Override
        public boolean isValid(Response r, Agent interactor) {
            return true;
        }

        @Override
        public boolean isValid(Choice c, Agent interactor) {
            return true;
        }
    }

    public static class DefaultConversationHandler extends ConversationHandler {
        public DefaultConversationHandler() {
            super(new ArrayList<DialogueTree>(), new DefaultDialogueVerifier(),
                    new DefaultOutcomeHandler());
        }
    }
}
