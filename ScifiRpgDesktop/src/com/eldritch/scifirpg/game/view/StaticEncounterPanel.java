package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.eldritch.scifirpg.game.model.StaticEncounter;
import com.eldritch.scifirpg.game.model.StaticEncounterModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class StaticEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public StaticEncounterPanel(final StaticEncounterModel model) {
        super(new BorderLayout());
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");
        
        StaticEncounter encounter = model.getEncounter();
        builder.appendRow("fill:200dlu");
        builder.append(createArea(encounter.getDescription()));
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
        
        if (encounter.canRest()) {
            final JButton button = new JButton("Rest");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                }
            });
            buttonPanel.add(button);
        }
        
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
}
