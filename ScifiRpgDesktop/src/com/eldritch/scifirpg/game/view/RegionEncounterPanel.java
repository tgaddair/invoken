package com.eldritch.scifirpg.game.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.RegionEncounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.RegionParams.Cell;

public class RegionEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final RegionEncounter encounter;
    
    public RegionEncounterPanel(RegionEncounter encounter) {
        super(new GridLayout(0, encounter.getRowCount()));
        this.encounter = encounter;
        
        int i = 0;
        for (Cell cell : encounter.getCells()) {
            while (i < cell.getPosition()) {
                add(new JLabel(""));
                i++;
            }
            
            add(createView(cell));
            i++;
        }
        
        setPreferredSize(new Dimension(450, 800));
    }
    
    private JLabel createView(final Cell cell) {
        JLabel label = new JLabel(cell.getLocationId());
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                GamePanel gamePanel = Application.getApplication().getGamePanel();
                gamePanel.getModel().setLocation(cell.getLocationId());
                gamePanel.reloadLocation();
            }
        });
        
        return label;
    }
}
