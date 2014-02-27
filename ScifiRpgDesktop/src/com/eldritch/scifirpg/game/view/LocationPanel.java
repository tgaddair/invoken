package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.AbstractEncounter;
import com.eldritch.scifirpg.game.model.DecisionEncounter;
import com.eldritch.scifirpg.game.model.LocationModel;
import com.eldritch.scifirpg.game.model.LocationModel.LocationListener;
import com.eldritch.scifirpg.game.model.RegionEncounter;
import com.eldritch.scifirpg.game.model.StaticEncounter;
import com.eldritch.scifirpg.proto.Locations.Location;

public class LocationPanel extends JPanel implements LocationListener {
    private static final long serialVersionUID = 1L;
    private final LocationModel model;

    public LocationPanel(LocationModel model) {
        super(new BorderLayout());
        this.model = model;
        resetPanel();
    }

    private void resetPanel() {
        AbstractEncounter encounter = model.drawEncounter();
        JPanel encounterPanel = null;
        switch (encounter.getType()) {
            case STATIC:
                encounterPanel = new StaticEncounterPanel((StaticEncounter) model.drawEncounter());
                break;
            case DECISION:
                // encounterPanel = new
                // DecisionEncounterPanel((DecisionEncounter)
                // model.drawEncounter());
                break;
            case ACTOR:
                // encounterPanel = new ActorEncounterPanel((ActorEncounter)
                // model.drawEncounter());
                break;
            case REGION:
                encounterPanel = new RegionEncounterPanel((RegionEncounter) model.drawEncounter(),
                        model);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized Encounter type "
                        + encounter.getType());
        }
        add(new EncounterPanel(encounter, model, encounterPanel));
    }

    @Override
    public void locationChanged(Location loc) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                resetPanel();
                Application.getApplication().getFrame().revalidate();
            }
        });
    }
}
