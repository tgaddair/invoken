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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.game.model.actor.ActorEncounter;
import com.eldritch.scifirpg.game.model.actor.ActorEncounterModel;
import com.eldritch.scifirpg.game.model.actor.ActorEncounterModel.ActorEncounterListener;
import com.eldritch.scifirpg.game.model.actor.Npc;
import com.eldritch.scifirpg.game.model.ActionAugmentation;
import com.eldritch.scifirpg.game.util.EffectUtil.Result;
import com.eldritch.scifirpg.game.util.LineBreaker;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ActorEncounterPanel extends JPanel implements ActorEncounterListener {
    private static final long serialVersionUID = 1L;
    private final Set<Npc> actors = new LinkedHashSet<>();
    private final ActorEncounterModel model;
    private final InteriorPanel interiorPanel;
    private ActionAugmentation selected = null;
    private Actor target = null;
    
    public ActorEncounterPanel(ActorEncounterModel model) {
        super(new BorderLayout());
        this.model = model;
        
        ActorEncounter encounter = model.getEncounter();
        actors.addAll(model.getActors());
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");
        
        JTextArea area = createArea(encounter.getDescription());
        area.setBorder(null);
        area.setOpaque(false);
        builder.append(area);
        builder.nextLine();
        
        // Add all the actor cards
        StagePanel stagePanel = new StagePanel(actors);
        builder.append(stagePanel);
        
        // Add the interior panel
        interiorPanel = new InteriorPanel();
        builder.appendRow("fill:p:grow");
        builder.append(interiorPanel);
        builder.nextLine();
        
        // Add action buffer
        JPanel bufferPanel = new JPanel(new FlowLayout());
        for (ActionAugmentation aug : model.getPlayer().redrawActions()) {
            bufferPanel.add(createAugCard(aug));
        }
        builder.appendRow("100dlu");
        builder.append(bufferPanel);
        builder.nextLine();
        
        // Add the flee button
        if (encounter.canFlee()) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            final JButton button = new JButton("Flee");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    // TODO
                }
            });
            buttonPanel.add(button);
    
            builder.appendRow("center:p");
            builder.append(buttonPanel);
            builder.nextLine();
        }
        
        add(builder.getPanel());
        
        model.addListener(this);
    }
    
    private JLabel createAugCard(final ActionAugmentation aug) {
        final JLabel label = new JLabel(aug.getName());
        label.setBorder(getDefaultBorder());
        label.setPreferredSize(new Dimension(90, 120));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        
        // Add a click action listener
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                selected = aug;
                label.setBorder(getSelectedBorder());
                
                // Double-click -> invoke on self
                if (me.getClickCount() == 2) {
                    model.invoke(aug);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent me) {
                if (target != null) {
                    model.invoke(aug, target);
                }
                
                // Visual cleanup
                if (selected == aug) {
                    selected = null;
                }
                label.setBorder(getDefaultBorder());
            }
        });
        
        return label;
    }
    
    private JLabel createActorCard(final Npc actor) {
        final JLabel label = new JLabel(actor.getName());
        label.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1)));
        label.setPreferredSize(new Dimension(90, 120));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        
        // Add a click action listener
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                if (selected != null) {
                    label.setBorder(getSelectedBorder());
                }
                target = actor;
            }
            
            @Override
            public void mouseExited(MouseEvent me) {
                label.setBorder(getDefaultBorder());
                
                // Reset the target variable if not already claimed elsewhere
                if (target == actor) {
                    target = null;
                }
            }
            
            @Override
            public void mousePressed(MouseEvent me) {
                // Double-click -> invoke on self
                if (me.getClickCount() == 2) {
                    interiorPanel.dialoguePanel.beginDialogueWith(actor);
                }
            }
        });
        
        return label;
    }
    
    private class StagePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public StagePanel(Collection<Npc> actors) {
            super(new FlowLayout());
            
            for (Npc actor : actors) {
                add(createActorCard(actor));
            }
        }
    }
    
    private class InteriorPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final String DIALOGUE = "Dialogue";
        private static final String COMBAT = "Combat";
        private final DialoguePanel dialoguePanel;
        private final CombatPanel combatPanel;
        
        public InteriorPanel() {
            super(new CardLayout());
            
            dialoguePanel = new DialoguePanel();
            combatPanel = new CombatPanel();
            
            add(dialoguePanel, DIALOGUE);
            add(combatPanel, COMBAT);
        }
        
        public void show(String key) {
            CardLayout cl = (CardLayout) getLayout();
            cl.show(this, key);
        }
    }
    
    private class DialoguePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public DialoguePanel() {
            super(new BorderLayout());
            
            if (!actors.isEmpty()) {
                final Npc actor = actors.iterator().next();
                add(getPanelFor(actor, actor.getGreeting()));
            }
        }
        
        public void beginDialogueWith(Npc actor) {
            setDialogueFor(actor, actor.getGreeting());
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
            for (final Choice c : response.getChoiceList()) {
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
        private final JTextPane combatLog;

        public CombatPanel() {
            super(new BorderLayout());
            
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("fill:max(p; 100px):grow");
            
            combatLog = new JTextPane();
            combatLog.setEditable(false);
            combatLog.setOpaque(false);
            
            JScrollPane scrollPane = new JScrollPane(combatLog);
            scrollPane.setBorder(null);
            builder.append(scrollPane);
            builder.nextLine(); 
            
            add(builder.getPanel());
        }
        
        public void report(Result result) {
            String text = combatLog.getText() + "\n" + result.toString();
            combatLog.setText(text);
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

    @Override
    public void effectApplied(Result result) {
        interiorPanel.combatPanel.report(result);
    }

    @Override
    public void startedCombat() {
        interiorPanel.show(InteriorPanel.COMBAT);
    }

    @Override
    public void combatTurnComplete(Actor prev, Actor next) {
        // TODO Auto-generated method stub
    }

    @Override
    public void actorKilled(Actor actor) {
        // TODO Auto-generated method stub
    }

    @Override
    public void actorTargeted(Actor actor) {
        // TODO Auto-generated method stub
    }
}
