package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.MainPanel;
import com.eldritch.scifirpg.editor.tables.ActorTable;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.AugmentationTable;
import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.editor.tables.FactionStatusTable;
import com.eldritch.scifirpg.editor.tables.ItemTable;
import com.eldritch.scifirpg.editor.tables.SkillTable;
import com.eldritch.scifirpg.editor.tables.TraitTable;
import com.eldritch.scifirpg.editor.util.ProfessionUtil;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.ActorParams.FactionStatus;
import com.eldritch.invoken.proto.Actors.ActorParams.Gender;
import com.eldritch.invoken.proto.Actors.ActorParams.Skill;
import com.eldritch.invoken.proto.Actors.ActorParams.Species;
import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Confidence;
import com.eldritch.invoken.proto.Actors.NonPlayerActor.Trait;
import com.eldritch.invoken.proto.Augmentations.AugmentationProto;
import com.eldritch.invoken.proto.Disciplines.Profession;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ActorEditorPanel extends AssetEditorPanel<NonPlayerActor, ActorTable> {
	private static final long serialVersionUID = 1L;
	
	private final JTextField idField = new JTextField();
	private final JTextField nameField = new JTextField();
	private final JComboBox<Profession> professionBox = new JComboBox<Profession>(Profession.values());
	private final JComboBox<Species> speciesBox = new JComboBox<Species>(Species.values());
	private final JComboBox<Gender> genderBox = new JComboBox<Gender>(Gender.values());
	private final JTextField bodyField = new JTextField();
	private final JTextField levelField = new JTextField();
	private final AugmentationTable augmentationTable = new AugmentationTable();
	private final InventoryTable itemTable = new InventoryTable();
	private final SkillTable skillTable = new SkillTable();
	private final FactionStatusTable factionTable = new FactionStatusTable();
	private final DialogueTable dialogueTable = new DialogueTable();
	private final TraitTable traitTable = new TraitTable();
	private final JCheckBox uniqueCheck = new JCheckBox("", false);
	private final JCheckBox guardCheck = new JCheckBox("", false);
	private final JComboBox<Aggression> aggressionBox = new JComboBox<Aggression>(Aggression.values());
	private final JComboBox<Assistance> assistanceBox = new JComboBox<Assistance>(Assistance.values());
	private final JComboBox<Confidence> confidenceBox = new JComboBox<Confidence>(Confidence.values());

	public ActorEditorPanel(ActorTable owner, JFrame frame, Optional<NonPlayerActor> prev) {
		super(owner, frame, prev);
		
		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, p, 4dlu, p", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:pref:grow, 3dlu, p"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.border(Borders.DIALOG);
		CellConstraints cc = new CellConstraints();
		int r = 1;
		int c = 1;
		
		nameField.addActionListener(new NameTypedListener(idField));
		builder.addLabel("Name", cc.xy(c, r));
		builder.add(nameField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("ID", cc.xy(c, r));
		builder.add(idField, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Unique", cc.xy(c, r));
		builder.add(uniqueCheck, cc.xy(c + 2, r));
		r += 2;
		
		r += 2;
		builder.addLabel("Items", cc.xy(c, r));
		builder.add(new AssetTablePanel(itemTable), cc.xy(c + 2, r));
		r += 2;
		
//		builder.addLabel("Traits", cc.xy(c, r));
//		builder.add(new AssetTablePanel(traitTable), cc.xy(c + 2, r));
//		r += 2;
		
		builder.addLabel("Body", cc.xy(c, r));
		builder.add(bodyField, cc.xy(c + 2, r));
		r += 2;
		
		aggressionBox.setSelectedItem(Aggression.AGGRESSIVE);
		builder.addLabel("Aggression", cc.xy(c, r));
		builder.add(aggressionBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Guard", cc.xy(c, r));
		builder.add(guardCheck, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Augmentations", cc.xy(c, r));
		builder.add(new AssetTablePanel(augmentationTable), cc.xy(c + 2, r));
		r += 2;
		
		c = 5;
		r = 1;
		
		speciesBox.addActionListener(new SpeciesSelectionListener());
		builder.addLabel("Species", cc.xy(c, r));
		builder.add(speciesBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Gender", cc.xy(c, r));
		builder.add(genderBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Profession", cc.xy(c, r));
		builder.add(professionBox, cc.xy(c + 2, r));
		r += 2;
		
		levelField.addActionListener(new LevelEnteredListener());
		builder.addLabel("Level", cc.xy(c, r));
		builder.add(levelField, cc.xy(c + 2, r));
		r += 2;

		builder.addLabel("Skills", cc.xy(c, r));
		builder.add(new AssetTablePanel(skillTable), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Factions", cc.xy(c, r));
		builder.add(new AssetTablePanel(factionTable), cc.xy(c + 2, r));
		r += 2;

		assistanceBox.setSelectedItem(Assistance.LOYAL);
		builder.addLabel("Assistance", cc.xy(c, r));
		builder.add(assistanceBox, cc.xy(c + 2, r));
		r += 2;
		
		confidenceBox.setSelectedItem(Confidence.CAPABLE);
		builder.addLabel("Confidence", cc.xy(c, r));
		builder.add(confidenceBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Dialogue Tree", cc.xy(c, r));
		builder.add(new AssetTablePanel(dialogueTable), cc.xy(c + 2, r));
		
//		List<Response> dialogue = prev.isPresent() ? prev.get().getDialogue().getDialogueList() : new ArrayList<Response>();
//		builder.add(new DialogueEditor(dialogue), cc.xy(c + 2, r));
		r += 2;
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.add(saveButton, cc.xy(c + 4, r));
		
		if (prev.isPresent()) {
			NonPlayerActor asset = prev.get();
			
			// Actor params
			ActorParams params = asset.getParams();
			enableFieldFor(params.getSpecies());
			idField.setText(params.getId());
			nameField.setText(params.getName());
			speciesBox.setSelectedItem(params.getSpecies());
			if (params.hasGender()) {
				genderBox.setSelectedItem(params.getGender());
			}
			if (params.hasBodyType()) {
				bodyField.setText(params.getBodyType());
			}
			if (params.hasProfession()) {
				professionBox.setSelectedItem(params.getProfession());
			}
			levelField.setText(params.getLevel() + "");
			for (Skill skill : params.getSkillList()) {
				skillTable.addAsset(skill);
			}
			for (FactionStatus fs : params.getFactionStatusList()) {
				factionTable.addAsset(fs);
			}
			for (InventoryItem item : params.getInventoryItemList()) {
				itemTable.addAsset(item);
			}
			for (AugmentationProto aug : params.getKnownAugIdList()) {
				augmentationTable.addAsset(aug);
			}
			
			// NPC params
			uniqueCheck.setSelected(asset.getUnique());
			guardCheck.setSelected(asset.getGuard());
			for (DialogueTree tree : asset.getDialogueList()) {
				dialogueTable.addAsset(tree);
			}
			aggressionBox.setSelectedItem(asset.getAggression());
			assistanceBox.setSelectedItem(asset.getAssistance());
			confidenceBox.setSelectedItem(asset.getConfidence());
			for (Trait trait : asset.getTraitList()) {
				traitTable.addAsset(trait);
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(1300, 800));
	}

	@Override
	public NonPlayerActor createAsset() {
		ActorParams.Builder params = ActorParams.newBuilder()
				.setId(idField.getText())
				.setName(nameField.getText())
				.setSpecies((Species) speciesBox.getSelectedItem())
				.setLevel(Integer.parseInt(levelField.getText()))
				.addAllSkill(skillTable.getAssets())
				.addAllFactionStatus(factionTable.getAssets())
				.addAllInventoryItem(itemTable.getAssets())
				.addAllKnownAugId(augmentationTable.getAssets());
		if (genderBox.isEnabled()) {
			params.setGender((Gender) genderBox.getSelectedItem());
		}
		if (!bodyField.getText().isEmpty()) {
			params.setBodyType(bodyField.getText());
		}
		if (professionBox.isEnabled()) {
			params.setProfession((Profession) professionBox.getSelectedItem());
		}
		
		return NonPlayerActor.newBuilder()
				.setParams(params.build())
				.setUnique(uniqueCheck.isSelected())
				.setGuard(guardCheck.isSelected())
				.setAggression((Aggression) aggressionBox.getSelectedItem())
				.setAssistance((Assistance) assistanceBox.getSelectedItem())
				.setConfidence((Confidence) confidenceBox.getSelectedItem())
				.addAllDialogue(dialogueTable.getAssets())
				.addAllTrait(traitTable.getAssets())
				.build();
	}
	
	private void enableFieldFor(Species s) {
		boolean enabled = true;
		switch (s) {
			case HUMAN:
			case UNDEAD:
				break;
			default:
				enabled = false;
		}
	
		genderBox.setEnabled(enabled);
		professionBox.setEnabled(enabled);
	}
	
	private class SpeciesSelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Species s = (Species) speciesBox.getSelectedItem();
			enableFieldFor(s);
		}
	}
	
	protected class LevelEnteredListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField source = (JTextField) e.getSource();
			int level = Integer.parseInt(source.getText());
			Profession profession = (Profession) professionBox.getSelectedItem();
			
			skillTable.clearAssets();
			for (Skill asset : ProfessionUtil.getSkillsFor(profession, level)) {
				skillTable.addAsset(asset);
			}
		}
	}
	
	public static class InventoryTable extends AssetTable<InventoryItem> {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUMN_NAMES = { 
			"Item", "Count", "Variance", "Drop Chance" };
		
		public InventoryTable() {
			super(COLUMN_NAMES, "Faction Status");
		}

		@Override
		protected JPanel getEditorPanel(Optional<InventoryItem> prev, JFrame frame) {
			return new InventoryPanel(this, frame, prev);
		}
		
		@Override
		protected Object[] getDisplayFields(InventoryItem item) {
			return new Object[]{item.getItemId(), item.getCount(), item.getVariance(), item.getDropChance()};
		}
	}
	
	public static class InventoryPanel extends AssetEditorPanel<InventoryItem, InventoryTable> {
		private static final long serialVersionUID = 1L;
		
		private final JComboBox<String> pointerBox = new JComboBox<String>();
		private final JTextField countField = new JTextField("1");
		private final JTextField varianceField = new JTextField("0");
		private final JTextField dropField = new JTextField("1.0");
		
		public InventoryPanel(InventoryTable table, JFrame frame, Optional<InventoryItem> prev) {
			super(table, frame, prev);
			
			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");
			
			ItemTable majorTable = MainPanel.ITEM_TABLE;
			Set<String> currentIds = new HashSet<>();
			for (InventoryItem item : table.getAssets()) {
				currentIds.add(item.getItemId());
			}
			List<String> values = new ArrayList<>();
			for (String id : majorTable.getAssetIds()) {
				if ((prev.isPresent() && prev.get().getItemId().equals(id))
						|| !currentIds.contains(id)) {
					values.add(id);
				}
			}
			pointerBox.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[0])));
			builder.append("Item:", pointerBox);
			builder.nextLine();
			
			builder.append("Count:", countField);
			builder.nextLine();
			
			builder.append("Variance:", varianceField);
            builder.nextLine();
			
			builder.append("Drop Chance:", dropField);
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				InventoryItem asset = prev.get();
				pointerBox.setSelectedItem(asset.getItemId());
				countField.setText(asset.getCount() + "");
				varianceField.setText(asset.getVariance() + "");
				dropField.setText(asset.getDropChance() + "");
			}

			add(builder.getPanel());
		}

		@Override
		public InventoryItem createAsset() {
			String id = (String) pointerBox.getSelectedItem();
			return InventoryItem.newBuilder()
					.setItemId(id)
					.setCount(Integer.parseInt(countField.getText()))
					.setVariance(Integer.parseInt(varianceField.getText()))
					.setDropChance(Double.parseDouble(dropField.getText()))
					.build();
		}
	}
}
