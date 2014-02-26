package com.eldritch.scifirpg.game.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eldritch.scifirpg.game.model.StaticEncounter;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class StaticEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public StaticEncounterPanel(StaticEncounter encounter) {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("right:pref");
        builder.appendColumn("3dlu");
        builder.appendColumn("fill:max(pref; 100px)");
        
        builder.append(new JLabel(encounter.getDescription()));
        builder.nextLine();
        
        add(builder.getPanel());
    }
}
