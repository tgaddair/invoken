package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class Speak extends Selector<Npc> {
    private static final float DIALOGUE_BREAK_SECS = 10f;

    public Speak() {
        // find a valid dialogue candidate
        Sequence<Npc> startSequence = new Sequence<>();
        startSequence.addChild(new CanStartConversation());

        Sequence<Npc> greetSequence = new Sequence<Npc>();
        greetSequence.addChild(new CanInteract()); // skip pursue if we're within interact range
        greetSequence.addChild(new Greet()); // speak to the target

        // pursue them until we're within interaction range
        Selector<Npc> greetOrPursue = new Selector<Npc>();
        greetOrPursue.addChild(greetSequence); // greet if we can
        greetOrPursue.addChild(new Pursue()); // otherwise, we pursue

        Sequence<Npc> forcedSequence = Tasks.sequence(new CanForceDialogue(), new FindInteractor(),
                new SetLastTask("ForceDialogue"), greetOrPursue);
        Sequence<Npc> banterSequence = Tasks.sequence(new CanBanter(), new Banter(),
                new SetLastTask("Banter"));
        startSequence.addChild(Tasks.selector(forcedSequence, banterSequence));

        addChild(Tasks.sequence(new CanContinueConversation(), new SetLastTask(
                "ContinueConversation"), new Idle()));
        addChild(startSequence);
    }

    private static class CanContinueConversation extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.inDialogue();
        }
    }

    private static class CanStartConversation extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.canSpeak() && npc.getLocation().inCameraBounds(npc.getPosition());
        }
    }

    private static class CanForceDialogue extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.getDialogueHandler().hasForcedGreeting()
                    && npc.getVisibleNeighbors().contains(npc.getLocation().getPlayer());
        }
    }

    private static class FindInteractor extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            // forced greetings only apply to the player character
            Agent neighbor = npc.getLocation().getPlayer();
            Response greeting = npc.getDialogueHandler().getForcedGreeting(neighbor);
            if (greeting != null) {
                npc.setTarget(neighbor);
                return true;
            }
            return false;
        }
    }

    private static class CanInteract extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasTarget() && canInteract(npc, npc.getTarget());
        }
    }

    private static class Greet extends SuccessTask {
        @Override
        public void doFor(Npc npc) {
            npc.getTarget().beginInteraction(npc, true);
            npc.setTask(getClass().getSimpleName());
        }
    }

    private static class CanBanter extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            // take a break between banter, and make sure the player can listen in
            // for the player to be able to listen in, the NPC
            return npc.getLastDialogue() >= DIALOGUE_BREAK_SECS
                    && npc.getLocation().isVisibleOnScreen(npc);
        }
    }

    private static class Banter extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                if (!canInteract(npc, neighbor)) {
                    // unable to interact
                    continue;
                }

                Response greeting = npc.getDialogueHandler().getTargetedGreeting(neighbor);
                if (greeting != null) {
                    npc.setTarget(neighbor);
                    neighbor.beginInteraction(npc, true);
                    npc.setTask(getClass().getSimpleName());
                    npc.banterFor(neighbor, greeting);
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean canInteract(Npc npc, Agent target) {
        return target.isAlive() && npc.canInteract(target);
    }
}
