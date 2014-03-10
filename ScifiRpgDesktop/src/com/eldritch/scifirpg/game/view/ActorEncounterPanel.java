package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.game.model.actor.ActorEncounter;
import com.eldritch.scifirpg.game.model.actor.ActorEncounterModel;
import com.eldritch.scifirpg.game.model.actor.ActorState;
import com.eldritch.scifirpg.game.model.actor.Npc;
import com.eldritch.scifirpg.game.model.actor.Player;
import com.eldritch.scifirpg.game.model.ActiveAugmentation;
import com.eldritch.scifirpg.game.model.EncounterListener.ActorEncounterListener;
import com.eldritch.scifirpg.game.util.LineBreaker;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.game.util.Result.ResultCallback;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ActorEncounterPanel extends JPanel implements ActorEncounterListener, ResultCallback {
    private enum InteriorPanelType {
        DIALOGUE, COMBAT, ACTOR, AUGMENTATION, OUTCOME
    }
    
    private static final long serialVersionUID = 1L;
    private final Set<Npc> actors = new LinkedHashSet<>();
    private final ActorEncounterModel model;
    private final Player player;
    private final StagePanel stagePanel;
    private final InteriorPanel interiorPanel;
    private final BufferPanel bufferPanel;
    private final JButton continueButton;
    private ActiveAugmentation selected = null;
    private ActorState target = null;
    
    public ActorEncounterPanel(final ActorEncounterModel model) {
        super(new BorderLayout());
        this.model = model;
        player = model.getPlayer();
        
        ActorEncounter encounter = model.getEncounter();
        actors.addAll(model.getNpcs());
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");
        
        JTextArea area = createArea(encounter.getDescription());
        area.setBorder(null);
        area.setOpaque(false);
        builder.append(area);
        builder.nextLine();
        
        // Add all the actor cards
        stagePanel = new StagePanel(actors);
        builder.append(stagePanel);
        
        // Add the interior panel
        interiorPanel = new InteriorPanel();
        builder.appendRow("fill:180dlu");
        builder.append(interiorPanel);
        builder.nextLine();
        
        // Add action buffer
        bufferPanel = new BufferPanel();
        builder.appendRow("100dlu");
        builder.append(bufferPanel);
        builder.nextLine();
        
        // Add continue in location button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                model.nextEncounter();
            }
        });
        continueButton.setEnabled(model.canContinue());
        buttonPanel.add(continueButton);
        
        // Add the pass button
        if (encounter.canFlee()) {
            final JButton button = new JButton("Pass");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    model.passCombat(player);
                }
            });
            buttonPanel.add(button);
        }
        builder.appendRow("center:p");
        builder.append(buttonPanel);
        builder.nextLine();
        
        add(builder.getPanel());
        
        model.addListener(this);
    }
    
    private AugmentationLabel createAugCard(ActiveAugmentation aug) {
        return new AugmentationLabel(aug);
    }
    
    private ActorLabel<Player> createPlayerCard() {
        final ActorLabel<Player> label = new ActorLabel<Player>(model.getState(player), player);
        
        // Add a click action listener
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                label.setBorder(getSelectedBorder());
                
                if (SwingUtilities.isRightMouseButton(me)) {
                    // Show actor panel
                    interiorPanel.actorPanel.setActor(model.getState(player));
                    interiorPanel.push(InteriorPanelType.ACTOR);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent me) {
                label.setBorder(getDefaultBorder());
                
                // Stop showing augmentation panel
                if (SwingUtilities.isRightMouseButton(me)) {
                    interiorPanel.pop(InteriorPanelType.ACTOR);
                }
            }
        });
        
        return label;
    }
    
    private ActorLabel<Npc> createActorCard(final Npc actor) {
        final ActorState state = model.getState(actor);
        final ActorLabel<Npc> label = new ActorLabel<Npc>(state, actor);
        
        // Add a click action listener
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                if (selected != null) {
                    label.setBorder(getSelectedBorder());
                }
                target = state;
            }
            
            @Override
            public void mouseExited(MouseEvent me) {
                label.setBorder(getDefaultBorder());
                
                // Reset the target variable if not already claimed elsewhere
                if (target == state) {
                    target = null;
                }
            }
            
            @Override
            public void mousePressed(MouseEvent me) {
                label.setBorder(getSelectedBorder());
                
                // Double-click -> invoke on self
                if (me.getClickCount() == 2) {
                    // Can't initate dialogue in combat
                    if (interiorPanel.getCurrent() != InteriorPanelType.COMBAT) {
                        interiorPanel.show(InteriorPanelType.DIALOGUE);
                        interiorPanel.dialoguePanel.beginDialogueWith(actor);
                    }
                } else if (SwingUtilities.isRightMouseButton(me)) {
                    // Show actor panel
                    interiorPanel.actorPanel.setActor(state);
                    interiorPanel.push(InteriorPanelType.ACTOR);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent me) {
                // Visual cleanup
                label.setBorder(getDefaultBorder());
                
                // Stop showing actor panel
                if (SwingUtilities.isRightMouseButton(me)) {
                    interiorPanel.pop(InteriorPanelType.ACTOR);
                }
            }
        });
        
        return label;
    }
    
    private class BufferPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Map<ActiveAugmentation, AugmentationLabel> views = new HashMap<>();
        private final ActorLabel<Player> playerLabel;
        
        public BufferPanel() {
            super(new FlowLayout());
            
            DefaultFormBuilder leftBuilder = new DefaultFormBuilder(new FormLayout(""));
            leftBuilder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            leftBuilder.appendColumn("right:pref");
            
            DefaultFormBuilder rightBuilder = new DefaultFormBuilder(new FormLayout(""));
            rightBuilder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            rightBuilder.appendColumn("left:pref");
            
            boolean useLeft = true;
            for (ActiveAugmentation aug : player.getActions()) {
                AugmentationLabel view = createAugCard(aug);
                add(view);
                views.put(aug, view);
                
                // Assign the view to the appropriate panel
                if (useLeft) {
                    leftBuilder.append(view);
                    leftBuilder.nextLine();
                } else {
                    rightBuilder.append(view);
                    rightBuilder.nextLine();
                }
                useLeft = !useLeft;
            }
            
            playerLabel = createPlayerCard();
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(playerLabel);
            
            add(leftBuilder.build());
            add(panel);
            add(rightBuilder.build());
        }
        
        public void update(ActiveAugmentation action) {
            views.get(action).update();
        }
        
        public void addAll(Collection<ActiveAugmentation> actions) {
            for (ActiveAugmentation action : actions) {
                AugmentationLabel view = createAugCard(action);
                add(view);
                views.put(action, view);
            }
        }
        
        @Override
        public void setEnabled(boolean enabled) {
            for (JLabel label : views.values()) {
                label.setEnabled(enabled);
            }
        }
    }
    
    private class StagePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Map<Actor, ActorLabel<?>> labels = new LinkedHashMap<>();
        
        public StagePanel(Collection<Npc> actors) {
            super(new FlowLayout());
            
            for (Npc actor : actors) {
                ActorLabel<?> label = createActorCard(actor);
                labels.put(actor, label);
                add(label);
            }
        }
        
        public void update(Result result) {
            // Check in case the actor was removed (killed or otherwise)
            if (labels.containsKey(result.getActor())) {
                labels.get(result.getActor()).update(result);
            }
        }
        
        public final void reset(Collection<Npc> actors) {
            labels.clear();
            removeAll();
            for (Npc actor : actors) {
                ActorLabel<?> label = createActorCard(actor);
                labels.put(actor, label);
                add(label);
            }
            Application.getApplication().getFrame().revalidate();
            repaint();
        }
    }
    
    private class InteriorPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final DialoguePanel dialoguePanel;
        private final CombatPanel combatPanel;
        private final ActorInfoPanel actorPanel;
        private final AugmentationInfoPanel augmentationPanel;
        private final OutcomePanel outcomePanel;
        private InteriorPanelType currentKey;
        private InteriorPanelType pushed;
        
        public InteriorPanel() {
            super(new CardLayout());
            
            dialoguePanel = new DialoguePanel();
            combatPanel = new CombatPanel();
            actorPanel = new ActorInfoPanel();
            augmentationPanel = new AugmentationInfoPanel();
            outcomePanel = new OutcomePanel();
            
            add(dialoguePanel, InteriorPanelType.DIALOGUE.name());
            add(combatPanel, InteriorPanelType.COMBAT.name());
            add(actorPanel, InteriorPanelType.ACTOR.name());
            add(augmentationPanel, InteriorPanelType.AUGMENTATION.name());
            add(outcomePanel, InteriorPanelType.OUTCOME.name());
            
            currentKey = InteriorPanelType.DIALOGUE;
            pushed = InteriorPanelType.DIALOGUE;
        }
        
        public InteriorPanelType getCurrent() {
            return currentKey;
        }
        
        public void push(InteriorPanelType key) {
            pushed = currentKey;
            show(key);
        }
        
        public void pop(InteriorPanelType key) {
            if (currentKey == key) {
                show(pushed);
            }
        }
        
        public void show(InteriorPanelType key) {
            currentKey = key;
            CardLayout cl = (CardLayout) getLayout();
            cl.show(this, key.name());
        }
    }
    
    /**
     * After winning a fight, this panel shows the player what they won.
     */
    private class OutcomePanel extends JPanel {
        private static final long serialVersionUID = 1L;
    }
    
    /**
     * Known information about the given actor on hover. 
     */
    private class ActorInfoPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public ActorInfoPanel() {
            super(new BorderLayout());
        }
        
        public void setActor(ActorState actor) {
            removeAll();
            add(getPanelFor(actor));
            repaint();
        }
        
        private JPanel getPanelFor(ActorState actor) {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("left:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");
            
            builder.append("Name:", new JLabel(actor.getActor().getName()));
            builder.nextLine();
            
            boolean scanned = false;
            if (scanned) {
                
            } else {
                builder.append("Injuries:", new JLabel(actor.getInjuries() + ""));
                builder.nextLine();
            }
            
            return builder.build();
        }
    }
    
    /**
     * Known information about the given augmentation on hover. 
     */
    private class AugmentationInfoPanel extends JPanel {
        private static final long serialVersionUID = 1L;
    }
    
    private class DialoguePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public DialoguePanel() {
            super(new BorderLayout());
            
            if (!actors.isEmpty()) {
                Iterator<Npc> it = actors.iterator();
                Npc actor = it.next();
                while (it.hasNext() && !actor.hasGreeting()) {
                    actor = it.next();
                }
                
                // Found an actor with a greeting, so let's initiate conversation
                if (actor.hasGreeting()) {
                    add(getPanelFor(actor, actor.getGreeting()));
                }
            }
        }
        
        public void beginDialogueWith(Npc actor) {
            if (actor != null && actor.hasGreeting()) {
                setDialogueFor(actor, actor.getGreeting());
            } else {
                reset();
            }
        }
        
        public void reset() {
            removeAll();
            Application.getApplication().getFrame().revalidate();
        }
        
        public void setDialogueFor(Npc actor, Response response) {
            removeAll();
            add(getPanelFor(actor, response));
            Application.getApplication().getFrame().revalidate();
        }
        
        private JPanel getPanelFor(final Npc actor, Response response) {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("fill:max(p; 100px):grow");
            
            JTextArea greetArea = createArea(response.getText());
            greetArea.setBorder(null);
            greetArea.setOpaque(false);
            builder.append(actor.getName(), greetArea);
            builder.nextLine();
            
            ButtonGroup group = new ButtonGroup();
            for (final Choice c : actor.getChoicesFor(response)) {
                String text = LineBreaker.breakUp(c.getText());
                JRadioButton radio = new JRadioButton("<html>" + text + "</html>");
                radio.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        setDialogueFor(actor, actor.getResponseFor(c));
                    }
                });
                group.add(radio);
                
                builder.append(radio);
                builder.nextLine();
            }
            
            return builder.getPanel();
        }
    }
    
    private class CombatPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final JLabel combatLog;
        private final BlockingQueue<Result> queue = new LinkedBlockingQueue<Result>();
        private String lastText;
        private Actor lastActor;

        public CombatPanel() {
            super(new BorderLayout());
            
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("fill:max(p; 100px):grow");
            
            combatLog = new JLabel();
            combatLog.setFont(combatLog.getFont().deriveFont(16.0f));
            combatLog.setHorizontalAlignment(SwingConstants.CENTER);
            combatLog.setVerticalAlignment(SwingConstants.CENTER);
            builder.appendRow("fill:p:grow");
            builder.append(combatLog);
            builder.nextLine(); 
            
            add(builder.getPanel());
            
            scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        Result result = queue.take();
                        result.process(ActorEncounterPanel.this);
                        
                        String logText = result.toString();
                        if (result.getActor() == lastActor) {
                            logText = lastText + "<br/>" + logText;
                        }
                        lastText = logText;
                        lastActor = result.getActor();
                        
                        combatLog.setText("<html>" + logText + "</html>");
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
        
        public void clear() {
            combatLog.setText("<html></html>");
        }
        
        public void report(Result result) {
            queue.add(result);
        }
        
        public void report(Collection<Result> results) {
            queue.addAll(results);
        }
    }
    
    private Border getDefaultBorder() {
        return new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1));
    }
    
    private Border getSelectedBorder() {
        return new CompoundBorder(new LineBorder(Color.BLUE),
                new EmptyBorder(3, 5, 3, 3));
    }
    
    private final JTextArea createArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1)));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        return area;
    }
    
    private void enableBuffer(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                bufferPanel.setEnabled(enabled);
            }
        });
    }
    
    private void updateActorWith(final Result result) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (result.getActor() == player) {
                    bufferPanel.playerLabel.update(result);
                } else {
                    stagePanel.update(result);
                }
            }
        });
    }
    
    @Override
    public void outcomesApplied(List<Outcome> outcomes) {
        // TODO
        //interiorPanel.outcomesPanel.report(outcomes);
    }

    @Override
    public void effectApplied(Result result) {
        //interiorPanel.combatPanel.report(result);
        //updateActors();
    }

    @Override
    public void startedCombat() {
        enableBuffer(false);
        interiorPanel.dialoguePanel.reset();
        interiorPanel.show(InteriorPanelType.COMBAT);
    }
    
    @Override
    public void endedCombat() {
        enableBuffer(true);
        interiorPanel.combatPanel.clear();
        interiorPanel.show(InteriorPanelType.OUTCOME);
    }

    @Override
    public void combatTurnStarted(Actor actor) {
        enableBuffer(false);
        interiorPanel.combatPanel.report(new TurnResult(actor, true));
    }
    
    @Override
    public void actionRequested(Actor actor) {
        if (actor == player) {
            interiorPanel.combatPanel.report(new EnabledResult(actor));
        }
    }
    
    @Override
    public void handleResults(List<Result> results) {
        interiorPanel.combatPanel.report(results);
    }

    @Override
    public void combatTurnPassed(Actor current) {
        interiorPanel.combatPanel.report(new Result(current, "PASS"));
    }
    
    @Override
    public void combatTurnEnded(Actor actor) {
        interiorPanel.combatPanel.report(new TurnResult(actor, false));
    }

    @Override
    public void actorKilled(Actor actor) {
        actors.remove(actor);
        stagePanel.reset(actors);
    }

    @Override
    public void actorTargeted(Actor actor) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void actionUsed(ActiveAugmentation action) {
        if (action.getOwner() == player) {
            bufferPanel.update(action);
        }
    }
    
    @Override
    public void actionsDrawn(Actor actor, Set<ActiveAugmentation> actions) {
        if (actor == player) {
            bufferPanel.addAll(actions);
        }
    }
    
    @Override
    public void canContinue(final boolean can) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                continueButton.setEnabled(can);
            }
        });
    }
    
    @Override
    public void playerKilled() {
        JPanel panel = Application.getApplication().getMainPanel().getGameOverPanel();
        Application.getApplication().setPanel(panel);
    }
    
    public class AugmentationLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private final ActiveAugmentation aug;
        
        public AugmentationLabel(final ActiveAugmentation aug) {
            super(getBriefText(aug));
            this.aug = aug;
            
            setBorder(getDefaultBorder());
            setPreferredSize(new Dimension(55, 55));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBackground(Color.WHITE);
            setOpaque(true);
            
            // Add a click action listener
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent me) {
                    selected = aug;
                    AugmentationLabel.this.setBorder(getSelectedBorder());
                    
                    // Double-click -> invoke on self
                    if (me.getClickCount() == 2) {
                        model.takeAction(aug, player);
                    } else if (SwingUtilities.isRightMouseButton(me)) {
                        // Show augmentation panel
                        interiorPanel.push(InteriorPanelType.AUGMENTATION);
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent me) {
                    if (target != null) {
                        model.takeAction(aug, player, target);
                    }
                    
                    // Visual cleanup
                    if (selected == aug) {
                        selected = null;
                    }
                    AugmentationLabel.this.setBorder(getDefaultBorder());
                    
                    // Stop showing augmentation panel
                    if (SwingUtilities.isRightMouseButton(me)) {
                        interiorPanel.pop(InteriorPanelType.AUGMENTATION);
                    }
                }
            });
        }
        
        public void update() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setText(getBriefText(aug));
                }
            });
        }
    }
    
    public class ActorLabel<T extends Actor> extends JLabel {
        private static final long serialVersionUID = 1L;
        private final ActorState state;
        private final T actor;
        
        public ActorLabel(ActorState state, T actor) {
            super(actor.getName());
            this.state = state;
            this.actor = actor;
            
            setBorder(getDefaultBorder());
            setPreferredSize(new Dimension(90, 120));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBackground(Color.WHITE);
            setOpaque(true);
            //setForeground(Color.green);
        }
        
        public T getActor() {
            return actor;
        }
        
        public synchronized void update(Result result) {
            // This is horrible, but this UI is only a prototype
            if (result instanceof TurnResult) {
                TurnResult tr = (TurnResult) result;
                setTurn(tr.isStart());
            } else {
                setTextFor(result);
            }
        }
        
        private void setTurn(boolean start) {
            setBorder(start ? getTurnBorder() : getDefaultBorder());
        }
        
        private void setTextFor(Result result) {
            setText(result.toString());
            
            int delay = 3000; // milliseconds
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    update();
                }
            };
            Timer t = new Timer(delay, taskPerformer);
            t.setRepeats(false);
            t.start();
        }
        
        public void update() {
            setText(actor.getName());
            
            float r = (1.0f * state.getCurrentHealth()) / actor.getBaseHealth();
            setBackground(new Color(r, r, r));
        }
        
        private Border getTurnBorder() {
            return new CompoundBorder(new LineBorder(Color.GREEN),
                    new EmptyBorder(3, 5, 3, 3));
        }
    }
    
    private static String getBriefText(ActiveAugmentation aug) {
        return "<html><div style=\"text-align: center;\">"
              + aug.getName() + "<br/>" + aug.getUses() + "</html>";
    }
    
    public void handleResult(Result result) {
        updateActorWith(result);
    }
    
    public class EnabledResult extends Result {
        public EnabledResult(Actor actor) {
            super(actor, "");
        }
        
        @Override
        public void process(ResultCallback callback) {
            enableBuffer(true);
        }
    }
    
    public class TurnResult extends Result {
        private final boolean start;
        
        public TurnResult(Actor actor, boolean start) {
            super(actor, "");
            this.start = start;
        }
        
        public boolean isStart() {
            return start;
        }
    }
}
