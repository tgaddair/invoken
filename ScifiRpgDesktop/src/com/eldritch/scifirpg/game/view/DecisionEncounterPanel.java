package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.DecisionEncounterModel;
import com.eldritch.scifirpg.game.util.LineBreaker;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Choice;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DecisionEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public DecisionEncounterPanel(final DecisionEncounterModel model) {
        super(new BorderLayout());
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");
        
        builder.appendRow("fill:200dlu");
        builder.append(new DecisionPanel(model));
        builder.nextLine();
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                model.nextEncounter();
            }
        });
        continueButton.setEnabled(model.canContinue());
        buttonPanel.add(continueButton);
        
        builder.appendRow("center:p");
        builder.append(buttonPanel);
        builder.nextLine();
        
        add(builder.getPanel());
        
        model.init();
    }
    
    private final JTextArea createArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1)));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBorder(null);
        area.setOpaque(false);
        return area;
    }
    
    private class DecisionPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final DecisionEncounterModel model;

        public DecisionPanel(DecisionEncounterModel model) {
            super(new BorderLayout());
            this.model = model;
            
            if (model.hasGreeting()) {
                add(getPanelFor(model.getGreeting()));
            }
        }
        
        public void setDialogueFor(Response response) {
            removeAll();
            add(getPanelFor(response));
            Application.getApplication().getFrame().revalidate();
        }
        
        private JPanel getPanelFor(Response response) {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("fill:max(p; 100px):grow");
            
            JTextArea greetArea = createArea(response.getText());
            greetArea.setBorder(null);
            greetArea.setOpaque(false);
            builder.append(greetArea);
            builder.nextLine();
            
            ButtonGroup group = new ButtonGroup();
            for (final Choice c : model.getChoicesFor(response)) {
                String text = LineBreaker.breakUp(c.getText());
                JRadioButton radio = new JRadioButton("<html>" + text + "</html>");
                radio.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        setDialogueFor(model.getResponseFor(c));
                    }
                });
                group.add(radio);
                
                builder.append(radio);
                builder.nextLine();
            }
            
            return builder.getPanel();
        }
    }
}
