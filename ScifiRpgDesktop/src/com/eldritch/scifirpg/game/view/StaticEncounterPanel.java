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
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class StaticEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public StaticEncounterPanel(StaticEncounter encounter) {
        super(new BorderLayout());
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");
        
        builder.appendRow("fill:200dlu");
        builder.append(createArea(encounter.getDescription()));
        builder.nextLine();
        
        if (encounter.canRest()) {
            JPanel buttonPanel = new JPanel(new FlowLayout());
            final JButton returnButton = new JButton("Rest");
            returnButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                }
            });
            buttonPanel.add(returnButton);
    
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
