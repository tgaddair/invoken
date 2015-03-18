package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class Speak extends Sequence<Npc> {
    public Speak() {
        // find a valid dialogue candidate
        addChild(new CanStartConversation());
        addChild(new FindInteractor());
        
        Sequence<Npc> greetSequence = new Sequence<Npc>();
        greetSequence.addChild(new CanInteract());  // skip pursue if we're within interact range
        greetSequence.addChild(new Greet());  // speak to the target
        
        // pursue them until we're within interaction range
        Selector<Npc> selector = new Selector<Npc>();
        selector.addChild(greetSequence);  // greet if we can
        selector.addChild(new Pursue());  // otherwise, we pursue
        addChild(selector);
    }
    
    private static class CanStartConversation extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.inDialogue();
        }
    }
    
    private static class FindInteractor extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            for (Agent neighbor : npc.getVisibleNeighbors()) {
                Response greeting = npc.getDialogueHandler().getForcedGreeting(neighbor);
                if (greeting != null) {
                    npc.setTarget(neighbor);
                    return true;
                }
            }
            return false;
        }
    }
    
    private static class CanInteract extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasTarget() && npc.canInteract(npc.getTarget());
        }
    }
    
    private static class Greet extends LeafTask<Npc> {
        @Override
        public void run(Npc npc) {
            npc.getTarget().beginDialogue(npc, true);
            npc.setTask(getClass().getSimpleName());
            success();
        }

        @Override
        protected Task<Npc> copyTo(Task<Npc> task) {
            return task;
        }
    }
}
