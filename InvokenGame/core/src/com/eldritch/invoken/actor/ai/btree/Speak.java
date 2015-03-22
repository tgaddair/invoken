package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class Speak extends Sequence<Npc> {
    private static final float DIALOGUE_BREAK_SECS = 10f;
    
    @SuppressWarnings("unchecked")
    public Speak() {
        // find a valid dialogue candidate
        addChild(new CanStartConversation());
        
        Sequence<Npc> greetSequence = new Sequence<Npc>();
        greetSequence.addChild(new CanInteract());  // skip pursue if we're within interact range
        greetSequence.addChild(new Greet());  // speak to the target
        
        // pursue them until we're within interaction range
        Selector<Npc> greetOrPursue = new Selector<Npc>();
        greetOrPursue.addChild(greetSequence);  // greet if we can
        greetOrPursue.addChild(new Pursue());  // otherwise, we pursue
        
        Sequence<Npc> forcedSequence = Tasks.sequence(new FindInteractor(), greetOrPursue);
        Sequence<Npc> banterSequence = Tasks.sequence(new CanBanter(), new Banter());
        addChild(Tasks.selector(forcedSequence, banterSequence));
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
            return npc.hasTarget() && canInteract(npc, npc.getTarget());
        }
    }
    
    private static class Greet extends SuccessTask {
        @Override
        public void doFor(Npc npc) {
            npc.getTarget().beginDialogue(npc, true);
            npc.setTask(getClass().getSimpleName());
        }
    }
    
    private static class CanBanter extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            // take a break between banter, and make sure the player can listen in
            return npc.getLastDialogue() >= DIALOGUE_BREAK_SECS 
                    && npc.isNeighbor(npc.getLocation().getPlayer());
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
                    neighbor.beginDialogue(npc, true);
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
