package com.eldritch.scifirpg.game.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.eldritch.scifirpg.game.Application;
import com.eldritch.scifirpg.game.model.AbstractEncounter;
import com.eldritch.scifirpg.game.model.GameState;
import com.eldritch.scifirpg.game.model.LocationModel;
import com.eldritch.scifirpg.game.model.LocationModel.LocationListener;
import com.eldritch.scifirpg.game.model.actor.ActorEncounter;
import com.eldritch.scifirpg.game.model.RegionEncounter;
import com.eldritch.scifirpg.game.model.StaticEncounter;
import com.eldritch.scifirpg.proto.Locations.Location;

public class LocationPanel extends JPanel implements LocationListener {
    private static final long serialVersionUID = 1L;
    private final GameState state;
    private final LocationModel model;

    public LocationPanel(GameState state, LocationModel model) {
        super(new BorderLayout());
        this.state = state;
        this.model = model;
        addPanel();
    }

    private void addPanel() {
        AbstractEncounter encounter = model.getCurrentEncounter();
        JPanel encounterPanel = null;
        switch (encounter.getType()) {
            case STATIC:
                encounterPanel = new StaticEncounterPanel(
                        ((StaticEncounter) encounter).createModel(state));
                break;
            case DECISION:
                // encounterPanel = new
                // DecisionEncounterPanel((DecisionEncounter)
                // encounter);
                break;
            case ACTOR:
                encounterPanel = new ActorEncounterPanel(
                        ((ActorEncounter) encounter).createModel(state));
                break;
            case REGION:
                encounterPanel = new RegionEncounterPanel((RegionEncounter) encounter,
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
                addPanel();
                Application.getApplication().getFrame().revalidate();
            }
        });
    }
}
