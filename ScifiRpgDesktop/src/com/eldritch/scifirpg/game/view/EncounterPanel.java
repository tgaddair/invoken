package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.eldritch.scifirpg.game.model.AbstractEncounter;
import com.eldritch.scifirpg.game.model.LocationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LocationModel locationModel;

    public EncounterPanel(AbstractEncounter encounter, final LocationModel locationModel,
            JPanel subpanel) {
        super(new BorderLayout());
        this.locationModel = locationModel;

        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("fill:max(p; 100px):grow");

        JLabel title = new JLabel(encounter.getTitle());
        title.setFont(title.getFont().deriveFont(24.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        builder.appendRow("fill:50dlu");
        builder.append(title);
        builder.nextLine();

        builder.appendRow("fill:200dlu:grow");
        builder.append(subpanel);
        builder.nextLine();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        final JButton returnButton = new JButton("Return");
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                locationModel.returnToPreviousLocation();
            }
        });
        buttonPanel.add(returnButton);

        builder.appendRow("center:p");
        builder.append(buttonPanel);
        builder.nextLine();

        add(builder.getPanel());
    }
}
