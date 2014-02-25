package com.eldritch.scifirpg.editor.panel;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.tables.EncounterTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable.EncounterOutcomeTable;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.proto.Actors.DialogueTree;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.scifirpg.proto.Locations.Encounter.DecisionParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.RegionParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.RegionParams.Cell;
import com.eldritch.scifirpg.proto.Locations.Encounter.StaticParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.Type;
import com.eldritch.scifirpg.proto.Outcomes.Outcome;
import com.eldritch.scifirpg.proto.Prerequisites.Prerequisite;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class EncounterEditorPanel extends AssetEditorPanel<Encounter, EncounterTable> implements ItemListener {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextField titleField = new JTextField();
	private final JComboBox<Type> typeBox = new JComboBox<Type>(Type.values());
	private final JTextField weightField = new JTextField();
	private final JCheckBox uniqueCheck = new JCheckBox();
	private final JCheckBox returnCheck = new JCheckBox();
	private final JComboBox<String> successorBox = new JComboBox<String>();
	private final PrerequisiteTable prereqTable = new PrerequisiteTable();
	
	// Different cards for different types
	private final JPanel cards;
	private final StaticEncounterPanel staticPanel = new StaticEncounterPanel();
	private final DecisionEncounterPanel decisionPanel = new DecisionEncounterPanel();
	private final ActorEncounterPanel actorPanel = new ActorEncounterPanel();
	private final RegionEncounterPanel regionPanel = new RegionEncounterPanel();

	public EncounterEditorPanel(EncounterTable owner, JFrame frame, Optional<Encounter> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = createFormBuilder();
		titleField.addActionListener(new NameTypedListener(idField));
		builder.append("Title:", titleField);
		builder.nextLine();
		
		builder.append("ID:", idField);
		builder.nextLine();
		
		typeBox.addItemListener(this);
		builder.append("Type:", typeBox);
		builder.nextLine();
		
		weightField.setText("1.0");
		builder.append("Weight:", weightField);
		builder.nextLine();
		
		builder.append("Unique:", uniqueCheck);
		builder.nextLine();
		
		builder.append("Return:", returnCheck);
		builder.nextLine();
		
		List<String> values = new ArrayList<>();
		values.add("");
		for (String id : owner.getAssetIds()) {
			if (!prev.isPresent() || !prev.get().getId().equals(id)) {
				values.add(id);
			}
		}
		successorBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
		builder.append("Successor:", successorBox);
		builder.nextLine();
		
		builder.appendRow("fill:120dlu");
		builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
		builder.nextLine();
		
		cards = new JPanel(new CardLayout());
		cards.add(staticPanel, Type.STATIC.name());
		cards.add(decisionPanel, Type.DECISION.name());
		cards.add(actorPanel, Type.ACTOR.name());
		cards.add(regionPanel, Type.REGION.name());
		
		builder.appendRow("fill:p:grow");
		builder.append("Parameters:", cards);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Encounter asset = prev.get();
			idField.setText(asset.getId());
			titleField.setText(asset.getTitle());
			typeBox.setSelectedItem(asset.getType());
			weightField.setText(asset.getWeight() + "");
			uniqueCheck.setSelected(asset.getUnique());
			returnCheck.setSelected(asset.getReturn());
			if (asset.hasSuccessorId()) {
				successorBox.setSelectedItem(asset.getSuccessorId());
			}
			for (Prerequisite p : asset.getPrereqList()) {
				prereqTable.addAsset(p);
			}
			if (asset.hasStaticParams()) {
				staticPanel.setParams(asset.getStaticParams());
			}
			if (asset.hasDecisionParams()) {
				decisionPanel.setParams(asset.getDecisionParams());
			}
			if (asset.hasActorParams()) {
				actorPanel.setParams(asset.getActorParams());
			}
			if (asset.hasRegionParams()) {
				regionPanel.setParams(asset.getRegionParams());
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(800, 900));
	}

	@Override
	public Encounter createAsset() {
		Type t = (Type) typeBox.getSelectedItem();
		Encounter.Builder encounter = Encounter.newBuilder()
				.setId(idField.getText())
				.setTitle(titleField.getText())
				.setType(t)
				.setWeight(Integer.parseInt(weightField.getText()))
				.setUnique(uniqueCheck.isSelected())
				.setReturn(returnCheck.isSelected())
				.addAllPrereq(prereqTable.getAssets());
		String successorId = (String) successorBox.getSelectedItem();
		if (!successorId.isEmpty()) {
			encounter.setSuccessorId(successorId);
		}
		switch (t) {
			case STATIC:
				encounter.setStaticParams(staticPanel.getParams());
				break;
			case DECISION:
				encounter.setDecisionParams(decisionPanel.getParams());
				break;
			case ACTOR:
				encounter.setActorParams(actorPanel.getParams());
				break;
			case REGION:
				encounter.setRegionParams(regionPanel.getParams());
				break;
			default:
		}
		return encounter.build();
	}

	@Override
	public void itemStateChanged(ItemEvent ev) {
		CardLayout cl = (CardLayout) cards.getLayout();
		Type t = (Type) ev.getItem();
        cl.show(cards, t.name());
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
			return StaticParams.newBuilder()
					.setDescription(descriptionField.getText())
					.setRest(restCheck.isSelected())
					.addAllOutcome(outcomeTable.getAssets())
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
	
	private class DecisionEncounterPanel extends EncounterParamPanel<DecisionParams> {
		private static final long serialVersionUID = 1L;
		
		private final DialogueTable decisionTable = new DialogueTable();
		
		public DecisionEncounterPanel() {
			DefaultFormBuilder builder = createFormBuilder();
			builder.appendRow("fill:200dlu");
			builder.append("Decisions:", new AssetTablePanel(decisionTable));
			builder.nextLine();
			
			add(builder.getPanel());
		}
		
		@Override
		public DecisionParams getParams() {
			DialogueTree dt = DialogueTree.newBuilder()
					.addAllDialogue(decisionTable.getAssets())
					.build();
			return DecisionParams.newBuilder().setDecisionTree(dt).build();
		}

		@Override
		public void setParams(DecisionParams params) {
			for (Response r : params.getDecisionTree().getDialogueList()) {
				decisionTable.addAsset(r);
			}
		}
	}
	
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
			return RegionParams.newBuilder()
					.setRowLength(Integer.parseInt(lengthField.getText()))
					.addAllCell(cellTable.getAssets())
					.build();
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
		private static final String[] COLUMN_NAMES = { 
			"Location", "Position" };

		public CellTable() {
			super(COLUMN_NAMES, "Cell");
		}

		@Override
		protected JPanel getEditorPanel(Optional<Cell> asset, JFrame frame) {
			return new CellEditorPanel(this, frame, asset);
		}

		@Override
		protected Object[] getDisplayFields(Cell asset) {
			return new Object[]{asset.getLocationId(), asset.getPosition()};
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
			builder.nextLine();;

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
			return Cell.newBuilder()
					.setLocationId(id)
					.setPosition(Integer.parseInt(positionField.getText()))
					.addAllPrereq(prereqTable.getAssets())
					.build();
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
			return ActorParams.newBuilder()
					.setDescription(descriptionField.getText())
					.setNoDetect(noDetectCheck.isSelected())
					.setNoFlee(noFleeCheck.isSelected())
					.addAllActorScenario(actorTable.getAssets())
					.addAllOnFlee(outcomeTable.getAssets())
					.build();
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
	
	private static final String[] SCENARIO_COLUMN_NAMES = { 
		"Actor", "On Death" };
	
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
			return new Object[]{asset.getActorId(), outcomes};
		}
	}
	
	private class ActorScenarioPanel extends AssetEditorPanel<ActorScenario, ActorScenarioTable> {
		private static final long serialVersionUID = 1L;
		
		private final JComboBox<String> pointerBox = new JComboBox<String>();
		private final JCheckBox essentialCheck = new JCheckBox();
		private final OutcomeTable outcomeTable = new EncounterOutcomeTable(EncounterEditorPanel.this.getTable());
		private final DialogueTable dialogueTable = new DialogueTable();
		
		public ActorScenarioPanel(ActorScenarioTable table, JFrame frame, Optional<ActorScenario> prev) {
			super(table, frame, prev);
			
			Set<String> currentIds = new HashSet<>();
			for (ActorScenario scenario : table.getAssets()) {
				currentIds.add(scenario.getActorId());
			}
			
			List<String> values = new ArrayList<>();
			for (String id : MainPanel.ACTOR_TABLE.getAssetIds()) {
				if ((prev.isPresent() && prev.get().getActorId().equals(id))
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
				for (Outcome o : scenario.getOnDeathList()) {
					outcomeTable.addAsset(o);
				}
				if (scenario.hasDialogue()) {
					for (Response r : scenario.getDialogue().getDialogueList()) {
						dialogueTable.addAsset(r);
					}
				}
			}

			add(builder.getPanel());
		}

		@Override
		public ActorScenario createAsset() {
			String id = (String) pointerBox.getSelectedItem();
			ActorScenario.Builder as = ActorScenario.newBuilder()
					.setActorId(id)
					.setEssential(essentialCheck.isSelected())
					.addAllOnDeath(outcomeTable.getAssets());
			if (!dialogueTable.getAssets().isEmpty()) {
				DialogueTree dialogue = DialogueTree.newBuilder()
						.addAllDialogue(dialogueTable.getAssets())
						.build();
				as.setDialogue(dialogue);
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
