package com.eldritch.scifirpg.editor.panel;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.EncounterTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
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
	private final PrerequisiteTable prereqTable = new PrerequisiteTable();
	
	// Different cards for different types
	private final JPanel cards;
	private final StaticEncounterPanel staticPanel = new StaticEncounterPanel();
	private final ActorEncounterPanel actorPanel = new ActorEncounterPanel();

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
		
		builder.append("Weight:", weightField);
		builder.nextLine();
		
		builder.append("Unique:", uniqueCheck);
		builder.nextLine();
		
		builder.appendRow("fill:120dlu");
		builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
		builder.nextLine();
		
		cards = new JPanel(new CardLayout());
		cards.add(staticPanel, Type.STATIC.name());
		cards.add(actorPanel, Type.ACTOR.name());
		
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
			for (Prerequisite p : asset.getPrereqList()) {
				prereqTable.addAsset(p);
			}
			if (asset.hasStaticParams()) {
				staticPanel.setParams(asset.getStaticParams());
			}
			if (asset.hasActorParams()) {
				actorPanel.setParams(asset.getActorParams());
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(800, 900));
	}

	@Override
	public Encounter createAsset() {
		return null;
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
		private final OutcomeTable outcomeTable = new OutcomeTable();
		
		public StaticEncounterPanel() {
			DefaultFormBuilder builder = createFormBuilder();
			builder.append("Description:", descriptionField);
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
					.addAllOutcome(outcomeTable.getAssets())
					.build();
		}

		@Override
		public void setParams(StaticParams params) {
		}
	}
	
	private class ActorEncounterPanel extends EncounterParamPanel<ActorParams> {
		private static final long serialVersionUID = 1L;
		
		private final JTextArea descriptionField = createArea(true, 30, new Dimension(100, 100));
		private final ActorScenarioTable actorTable = new ActorScenarioTable();
		private final OutcomeTable outcomeTable = new OutcomeTable();
		
		public ActorEncounterPanel() {
			DefaultFormBuilder builder = createFormBuilder();
			builder.append("Description:", descriptionField);
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
					.addAllActorScenario(actorTable.getAssets())
					.addAllOnFlee(outcomeTable.getAssets())
					.build();
		}

		@Override
		public void setParams(ActorParams params) {
		}
	}
	
	private static DefaultFormBuilder createFormBuilder() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		return builder;
	}
	
	private static class ActorScenarioTable extends AssetTable<ActorScenario> {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUMN_NAMES = { 
			"Actor", "On Death" };

		public ActorScenarioTable() {
			super(COLUMN_NAMES, "Actor Scenario");
		}

		@Override
		protected JPanel getEditorPanel(Optional<ActorScenario> asset,
				JFrame frame) {
			return new JPanel();
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
	
	private abstract static class EncounterParamPanel<T extends Message> extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public abstract T getParams();
		
		public abstract void setParams(T params);
	}
}
