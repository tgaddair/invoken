package com.eldritch.scifirpg.editor.panel;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.AssetPointerTable;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.tables.EncounterTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable.EncounterOutcomeTable;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Location;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.proto.Locations.Encounter.RegionParams;
import com.eldritch.invoken.proto.Locations.Encounter.RegionParams.Cell;
import com.eldritch.invoken.proto.Locations.Encounter.StaticParams;
import com.eldritch.invoken.proto.Locations.Encounter.Type;
import com.eldritch.invoken.proto.Locations.Room;
import com.eldritch.invoken.proto.Outcomes.Outcome;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EncounterEditorPanel extends AssetEditorPanel<Encounter, EncounterTable> implements
        ItemListener {
    private static final long serialVersionUID = 1L;

    private final JTextField idField = new JTextField();
    private final JTextField titleField = new JTextField();
    // private final JComboBox<Type> typeBox = new JComboBox<Type>(Type.values());
    private final JTextField weightField = new JTextField();
    private final JCheckBox uniqueCheck = new JCheckBox();

    private final JComboBox<String> successorBox = new JComboBox<>();
    private final JComboBox<String> nextEncounterBox = new JComboBox<>();
    private final PrerequisiteTable prereqTable = new PrerequisiteTable();
    private final JComboBox<String> factionBox = new JComboBox<>();

    // Different cards for different types
    private final JPanel cards;
    private final StaticEncounterPanel staticPanel = new StaticEncounterPanel();
    // private final DecisionEncounterPanel decisionPanel = new
    // DecisionEncounterPanel();
    private final ActorEncounterPanel actorPanel = new ActorEncounterPanel();
    private final RegionEncounterPanel regionPanel = new RegionEncounterPanel();

    private final AssetPointerTable<Room> roomTable;

    public EncounterEditorPanel(EncounterTable owner, JFrame frame, Optional<Encounter> prev) {
        super(owner, frame, prev);
        this.roomTable = new AssetPointerTable<>(MainPanel.ROOM_TABLE);

        DefaultFormBuilder builder = createFormBuilder();
        titleField.addActionListener(new NameTypedListener(idField));
        builder.append("Title:", titleField);
        builder.nextLine();

        builder.append("ID:", idField);
        builder.nextLine();

        // typeBox.addItemListener(this);
        // builder.append("Type:", typeBox);
        // builder.nextLine();

        weightField.setText("1.0");
        builder.append("Weight:", weightField);
        builder.nextLine();

        builder.append("Unique:", uniqueCheck);
        builder.nextLine();

        List<String> fIds = new ArrayList<>();
        fIds.add("");
        fIds.addAll(MainPanel.LOCATION_TABLE.getAssetIds());
        successorBox.setModel(new DefaultComboBoxModel<>(fIds.toArray(new String[0])));
        successorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // setup the encounter box
                updateEncounterBox((String) successorBox.getSelectedItem());
            }
        });
        builder.append("Successor:", successorBox);
        builder.nextLine();

        List<String> eIds = new ArrayList<>();
        eIds.add("");
        nextEncounterBox.setModel(new DefaultComboBoxModel<>(eIds.toArray(new String[0])));
        builder.append("Encounter:", nextEncounterBox);
        builder.nextLine();

        builder.appendRow("fill:80dlu");
        builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
        builder.nextLine();

        builder.appendRow("fill:80dlu");
        builder.append("Rooms:", new AssetTablePanel(roomTable));
        builder.nextLine();

        List<String> factionIds = new ArrayList<>();
        factionIds.add("");
        factionIds.addAll(MainPanel.FACTION_TABLE.getAssetIds());
        factionBox.setModel(new DefaultComboBoxModel<>(factionIds.toArray(new String[0])));
        builder.append("Faction:", factionBox);
        builder.nextLine();

        cards = new JPanel(new CardLayout());
        cards.add(actorPanel, Type.ACTOR.name());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        builder.append(saveButton);
        builder.nextLine();

        if (prev.isPresent()) {
            Encounter asset = prev.get();
            idField.setText(asset.getId());
            titleField.setText(asset.getTitle());
            weightField.setText(asset.getWeight() + "");
            uniqueCheck.setSelected(asset.getUnique());

            for (Prerequisite p : asset.getPrereqList()) {
                prereqTable.addAsset(p);
            }
            if (asset.hasStaticParams()) {
                staticPanel.setParams(asset.getStaticParams());
            }
            if (asset.hasDecisionParams()) {
                // decisionPanel.setParams(asset.getDecisionParams());
            }
            if (asset.hasActorParams()) {
                actorPanel.setParams(asset.getActorParams());
            }
            if (asset.hasRegionParams()) {
                regionPanel.setParams(asset.getRegionParams());
            }
            for (String room : asset.getRoomIdList()) {
                roomTable.addAssetId(room);
            }
            if (asset.hasFactionId()) {
                factionBox.setSelectedItem(asset.getFactionId());
            }
        }

        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, builder.getPanel(), cards));
        setPreferredSize(new Dimension(1400, 800));
    }

    @Override
    public Encounter createAsset() {
        Encounter.Builder encounter = Encounter.newBuilder().setId(idField.getText())
                .setTitle(titleField.getText()).setType(Type.ACTOR)
                .setWeight(Double.parseDouble(weightField.getText()))
                .setUnique(uniqueCheck.isSelected()).addAllPrereq(prereqTable.getAssets())
                .addAllRoomId(roomTable.getAssetIds())
                .setFactionId((String) factionBox.getSelectedItem())
                .setActorParams(actorPanel.getParams());
        return encounter.build();
    }

    @Override
    public void itemStateChanged(ItemEvent ev) {
        CardLayout cl = (CardLayout) cards.getLayout();
        Type t = (Type) ev.getItem();
        cl.show(cards, t.name());
    }

    private void updateEncounterBox(String locationId) {
        List<String> eIds = new ArrayList<>();
        eIds.add("");

        Location location = MainPanel.LOCATION_TABLE.getAssetFor(locationId);
        if (location != null) {
            for (Encounter encounter : location.getEncounterList()) {
                eIds.add(encounter.getId());
            }
        }
        nextEncounterBox.setModel(new DefaultComboBoxModel<String>(eIds.toArray(new String[0])));
    }

    private class StaticEncounterPanel extends EncounterParamPanel<StaticParams> {
        private static final long serialVersionUID = 1L;

        private final JTextArea descriptionField = createArea(true, 30, new Dimension(100, 100));
        private final OutcomeTable outcomeTable = new EncounterOutcomeTable(getTable());
        private final JCheckBox restCheck = new JCheckBox();

        public StaticEncounterPanel() {
            DefaultFormBuilder builder = createFormBuilder();
            builder.append("Description:", descriptionField);
            builder.nextLine();

            builder.append("Rest:", restCheck);
            builder.nextLine();

            builder.appendRow("fill:120dlu");
            builder.append("Outcomes:", new AssetTablePanel(outcomeTable));
            builder.nextLine();

            add(builder.getPanel());
        }

        @Override
        public StaticParams getParams() {
            return StaticParams.newBuilder().setDescription(descriptionField.getText())
                    .setRest(restCheck.isSelected()).addAllOutcome(outcomeTable.getAssets())
                    .build();
        }

        @Override
        public void setParams(StaticParams params) {
            descriptionField.setText(params.getDescription());
            restCheck.setSelected(params.getRest());
            for (Outcome o : params.getOutcomeList()) {
                outcomeTable.addAsset(o);
            }
        }
    }

    //
    // private class DecisionEncounterPanel extends
    // EncounterParamPanel<DecisionParams> {
    // private static final long serialVersionUID = 1L;
    //
    // private final DialogueTable decisionTable = new DialogueTable();
    //
    // public DecisionEncounterPanel() {
    // DefaultFormBuilder builder = createFormBuilder();
    // builder.appendRow("fill:200dlu");
    // builder.append("Decisions:", new AssetTablePanel(decisionTable));
    // builder.nextLine();
    //
    // add(builder.getPanel());
    // }
    //
    // @Override
    // public DecisionParams getParams() {
    // DialogueTree dt = DialogueTree.newBuilder()
    // .addAllDialogue(decisionTable.getAsset())
    // .build();
    // return DecisionParams.newBuilder().setDecisionTree(dt).build();
    // }
    //
    // @Override
    // public void setParams(DecisionParams params) {
    // for (Response r : params.getDecisionTree().getDialogueList()) {
    // decisionTable.addAsset(r);
    // }
    // }
    // }

    private class RegionEncounterPanel extends EncounterParamPanel<RegionParams> {
        private static final long serialVersionUID = 1L;

        private final JTextArea lengthField = createArea(true, 30, new Dimension(100, 100));
        private final CellTable cellTable = new CellTable();

        public RegionEncounterPanel() {
            DefaultFormBuilder builder = createFormBuilder();
            builder.append("Row Length:", lengthField);
            builder.nextLine();

            builder.appendRow("fill:120dlu");
            builder.append("Cells:", new AssetTablePanel(cellTable));
            builder.nextLine();

            add(builder.getPanel());
        }

        @Override
        public RegionParams getParams() {
            return RegionParams.newBuilder().setRowLength(Integer.parseInt(lengthField.getText()))
                    .addAllCell(cellTable.getSortedAssets()).build();
        }

        @Override
        public void setParams(RegionParams params) {
            lengthField.setText(params.getRowLength() + "");
            for (Cell c : params.getCellList()) {
                cellTable.addAsset(c);
            }
        }
    }

    private static class CellTable extends AssetTable<Cell> {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = { "Location", "Position" };

        public CellTable() {
            super(COLUMN_NAMES, "Cell");
        }

        public List<Cell> getSortedAssets() {
            List<Cell> assets = new ArrayList<>(getAssets());
            Collections.sort(assets, new Comparator<Cell>() {
                @Override
                public int compare(Cell a1, Cell a2) {
                    return Integer.compare(a1.getPosition(), a2.getPosition());
                }
            });
            return assets;
        }

        @Override
        protected JPanel getEditorPanel(Optional<Cell> asset, JFrame frame) {
            return new CellEditorPanel(this, frame, asset);
        }

        @Override
        protected Object[] getDisplayFields(Cell asset) {
            return new Object[] { asset.getLocationId(), asset.getPosition() };
        }
    }

    private static class CellEditorPanel extends AssetEditorPanel<Cell, CellTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<String> pointerBox = new JComboBox<String>();
        private final JTextField positionField = new JTextField();
        private final PrerequisiteTable prereqTable = new PrerequisiteTable();

        public CellEditorPanel(CellTable table, JFrame frame, Optional<Cell> prev) {
            super(table, frame, prev);

            Set<String> currentIds = new HashSet<>();
            for (Cell c : table.getAssets()) {
                currentIds.add(c.getLocationId());
            }

            List<String> values = new ArrayList<>();
            for (String id : MainPanel.LOCATION_TABLE.getAssetIds()) {
                if ((prev.isPresent() && prev.get().getLocationId().equals(id))
                        || !currentIds.contains(id)) {
                    values.add(id);
                }
            }
            pointerBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));

            DefaultFormBuilder builder = createFormBuilder();
            builder.append("Location:", pointerBox);
            builder.nextLine();

            builder.append("Position:", positionField);
            builder.nextLine();

            builder.appendRow("fill:120dlu");
            builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
            builder.nextLine();
            ;

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Cell c = prev.get();
                pointerBox.setSelectedItem(c.getLocationId());
                positionField.setText(c.getPosition() + "");
                for (Prerequisite req : c.getPrereqList()) {
                    prereqTable.addAsset(req);
                }
            }

            add(builder.getPanel());
        }

        @Override
        public Cell createAsset() {
            String id = (String) pointerBox.getSelectedItem();
            return Cell.newBuilder().setLocationId(id)
                    .setPosition(Integer.parseInt(positionField.getText()))
                    .addAllPrereq(prereqTable.getAssets()).build();
        }
    }

    private class ActorEncounterPanel extends EncounterParamPanel<ActorParams> {
        private static final long serialVersionUID = 1L;

        private final JTextArea descriptionField = createArea(true, 30, new Dimension(100, 100));
        private final JCheckBox noDetectCheck = new JCheckBox();
        private final JCheckBox noFleeCheck = new JCheckBox();
        private final ActorScenarioTable actorTable = new ActorScenarioTable();
        private final OutcomeTable outcomeTable = new EncounterOutcomeTable(getTable());

        public ActorEncounterPanel() {
            DefaultFormBuilder builder = createFormBuilder();
            builder.append("Description:", descriptionField);
            builder.nextLine();

            builder.append("No Detect:", noDetectCheck);
            builder.nextLine();

            builder.append("No Flee:", noFleeCheck);
            builder.nextLine();

            builder.appendRow("fill:120dlu");
            builder.append("Actors:", new AssetTablePanel(actorTable));
            builder.nextLine();

            builder.appendRow("fill:120dlu");
            builder.append("On Flee:", new AssetTablePanel(outcomeTable));
            builder.nextLine();

            add(builder.getPanel());
        }

        @Override
        public ActorParams getParams() {
            return ActorParams.newBuilder().setDescription(descriptionField.getText())
                    .setNoDetect(noDetectCheck.isSelected()).setNoFlee(noFleeCheck.isSelected())
                    .addAllActorScenario(actorTable.getAssets())
                    .addAllOnFlee(outcomeTable.getAssets()).build();
        }

        @Override
        public void setParams(ActorParams params) {
            descriptionField.setText(params.getDescription());
            noDetectCheck.setSelected(params.getNoDetect());
            noFleeCheck.setSelected(params.getNoFlee());
            for (ActorScenario scenario : params.getActorScenarioList()) {
                actorTable.addAsset(scenario);
            }
            for (Outcome o : params.getOnFleeList()) {
                outcomeTable.addAsset(o);
            }
        }
    }

    private static final String[] SCENARIO_COLUMN_NAMES = { "Actor", "On Death" };

    private class ActorScenarioTable extends AssetTable<ActorScenario> {
        private static final long serialVersionUID = 1L;

        public ActorScenarioTable() {
            super(SCENARIO_COLUMN_NAMES, "Actor Scenario");
        }

        @Override
        protected JPanel getEditorPanel(Optional<ActorScenario> asset, JFrame frame) {
            return new ActorScenarioPanel(this, frame, asset);
        }

        @Override
        protected Object[] getDisplayFields(ActorScenario asset) {
            String outcomes = "";
            for (Outcome o : asset.getOnDeathList()) {
                outcomes += o.getType();
            }
            return new Object[] { asset.getActorId(), outcomes };
        }
    }

    private class ActorScenarioPanel extends AssetEditorPanel<ActorScenario, ActorScenarioTable> {
        private static final long serialVersionUID = 1L;

        private final JComboBox<String> pointerBox = new JComboBox<String>();
        private final JCheckBox essentialCheck = new JCheckBox();
        private final JCheckBox blockingCheck = new JCheckBox();
        private final JCheckBox aliveCheck = new JCheckBox("", true);
        private final JTextField minField = new JTextField();
        private final JTextField maxField = new JTextField();
        private final OutcomeTable outcomeTable = new EncounterOutcomeTable(
                EncounterEditorPanel.this.getTable());
        private final DialogueTable dialogueTable = new DialogueTable();

        public ActorScenarioPanel(ActorScenarioTable table, JFrame frame,
                Optional<ActorScenario> prev) {
            super(table, frame, prev);

            Set<String> currentIds = new HashSet<>();
            for (ActorScenario scenario : table.getAssets()) {
                currentIds.add(scenario.getActorId());
            }

            List<String> values = new ArrayList<>();
            for (NonPlayerActor actor : MainPanel.ACTOR_TABLE.getAssets()) {
                String id = actor.getParams().getId();
                if (!actor.getUnique()) {
                    values.add(id);
                } else if ((prev.isPresent() && prev.get().getActorId().equals(id))
                        || !currentIds.contains(id)) {
                    values.add(id);
                }
            }
            pointerBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));

            DefaultFormBuilder builder = createFormBuilder();
            builder.append("Actor:", pointerBox);
            builder.nextLine();

            builder.append("Essential:", essentialCheck);
            builder.nextLine();

            builder.append("Blocking:", blockingCheck);
            builder.nextLine();

            builder.append("Alive:", aliveCheck);
            builder.nextLine();

            builder.append("Min:", minField);
            builder.nextLine();

            builder.append("Max:", maxField);
            builder.nextLine();

            builder.appendRow("fill:120dlu");
            builder.append("On Death:", new AssetTablePanel(outcomeTable));
            builder.nextLine();

            builder.appendRow("fill:200dlu:grow");
            builder.append("Dialogue:", new AssetTablePanel(dialogueTable));
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                ActorScenario scenario = prev.get();
                pointerBox.setSelectedItem(scenario.getActorId());
                essentialCheck.setSelected(scenario.getEssential());
                blockingCheck.setSelected(scenario.getBlocking());
                aliveCheck.setSelected(scenario.getAlive());
                minField.setText(scenario.getMin() + "");
                maxField.setText(scenario.getMax() + "");
                for (Outcome o : scenario.getOnDeathList()) {
                    outcomeTable.addAsset(o);
                }
                if (scenario.hasDialogue()) {
                    dialogueTable.addAsset(scenario.getDialogue());
                }
            }

            add(builder.getPanel());
        }

        @Override
        public ActorScenario createAsset() {
            String id = (String) pointerBox.getSelectedItem();
            ActorScenario.Builder as = ActorScenario.newBuilder().setActorId(id)
                    .setEssential(essentialCheck.isSelected())
                    .setBlocking(blockingCheck.isSelected()).setAlive(aliveCheck.isSelected())
                    .addAllOnDeath(outcomeTable.getAssets());
            if (!minField.getText().isEmpty()) {
                as.setMin(Integer.parseInt(minField.getText()));
            }
            if (!maxField.getText().isEmpty()) {
                as.setMax(Integer.parseInt(maxField.getText()));
            }
            if (!dialogueTable.getAssets().isEmpty()) {
                as.setDialogue(dialogueTable.getAsset());
            }
            return as.build();
        }
    }

    private abstract static class EncounterParamPanel<T extends Message> extends JPanel {
        private static final long serialVersionUID = 1L;

        public abstract T getParams();

        public abstract void setParams(T params);
    }

    private static DefaultFormBuilder createFormBuilder() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("right:pref");
        builder.appendColumn("3dlu");
        builder.appendColumn("fill:max(pref; 100px)");
        return builder;
    }
}
