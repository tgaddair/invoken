package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.game.model.ActorEncounter;
import com.eldritch.scifirpg.game.model.ActorModel;
import com.eldritch.scifirpg.game.model.ActorModel.NpcState;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ActorEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public ActorEncounterPanel(ActorEncounter encounter, ActorModel model) {
        super(new BorderLayout());
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");
        
        JPanel actorPanel = new JPanel(new FlowLayout());
        for (NpcState actor : encounter.getActors(model)) {
            JLabel label = new JLabel(actor.getParams().getName());
            label.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                    new EmptyBorder(1, 3, 1, 1)));
            label.setPreferredSize(new Dimension(90, 120));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBackground(Color.WHITE);
            label.setOpaque(true);
            actorPanel.add(label);
        }
        builder.appendRow("fill:200dlu");
        builder.append(actorPanel);
        
        builder.appendRow("fill:100dlu");
        builder.append(createArea(encounter.getDescription()));
        builder.nextLine();
        
        if (encounter.canFlee()) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            final JButton button = new JButton("Flee");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                }
            });
            buttonPanel.add(button);
    
            builder.appendRow("center:p");
            builder.append(buttonPanel);
            builder.nextLine();
        }
        
        add(builder.getPanel());
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
}
