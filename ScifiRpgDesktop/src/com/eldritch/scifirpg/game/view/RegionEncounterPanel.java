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

import com.eldritch.scifirpg.game.model.LocationModel;
import com.eldritch.scifirpg.game.model.RegionEncounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.RegionParams.Cell;

public class RegionEncounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LocationModel model;
    
    public RegionEncounterPanel(RegionEncounter encounter, LocationModel model) {
        super(new GridLayout(0, encounter.getRowCount()));
        this.model = model;
        
        int i = 0;
        for (Cell cell : encounter.getCells()) {
            while (i < cell.getPosition()) {
                add(new JLabel(""));
                i++;
            }
            
            add(createView(cell));
            i++;
        }
        
        setPreferredSize(new Dimension(430, 800));
    }
    
    private JLabel createView(final Cell cell) {
        JLabel label = new JLabel(cell.getLocationId());
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                model.setCurrent(cell.getLocationId());
            }
        });
        
        return label;
    }
}
