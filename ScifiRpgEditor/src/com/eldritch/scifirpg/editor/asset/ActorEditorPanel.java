package com.eldritch.scifirpg.editor.asset;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.ActorTable;
import com.eldritch.scifirpg.editor.tables.EffectTable;
import com.eldritch.scifirpg.editor.tables.RequirementTable;
import com.eldritch.scifirpg.editor.tables.SkillTable;
import com.eldritch.scifirpg.editor.tables.TraitTable;
import com.eldritch.scifirpg.editor.util.ProfessionUtil;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Gender;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Profession;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Skill;
import com.eldritch.scifirpg.proto.Actors.ActorParams.Species;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Aggression;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Assistance;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor.Confidence;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ActorEditorPanel extends AssetEditorPanel<NonPlayerActor, ActorTable> {
	private static final long serialVersionUID = 1L;
	
	private final JTextField idField = new JTextField();
	private final JTextField nameField = new JTextField();
	private final JComboBox<Profession> professionBox = new JComboBox<Profession>(Profession.values());
	private final JComboBox<Species> speciesBox = new JComboBox<Species>(Species.values());
	private final JComboBox<Gender> genderBox = new JComboBox<Gender>(Gender.values());
	private final JTextField levelField = new JTextField();
	private final EffectTable augmentationTable = new EffectTable();
	private final RequirementTable itemTable = new RequirementTable();
	private final SkillTable skillTable = new SkillTable();
	private final RequirementTable factionTable = new RequirementTable();
	private final EffectTable dialogueTable = new EffectTable();
	private final TraitTable traitTable = new TraitTable();
	private final JCheckBox uniqueCheck = new JCheckBox("", false);
	private final JCheckBox speakCheck = new JCheckBox("", true);
	private final JComboBox<Aggression> aggressionBox = new JComboBox<Aggression>(Aggression.values());
	private final JComboBox<Assistance> assistanceBox = new JComboBox<Assistance>(Assistance.values());
	private final JComboBox<Confidence> confidenceBox = new JComboBox<Confidence>(Confidence.values());

	public ActorEditorPanel(ActorTable owner, JFrame frame, Optional<NonPlayerActor> prev) {
		super(owner, frame, prev);
		
		FormLayout layout = new FormLayout(
				"right:p, 4dlu, p, 7dlu, right:p, 4dlu, p, 4dlu, p", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		
		// Specify that columns 1 & 5 as well as 3 & 7 have equal widths.       
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
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
		builder.addLabel("Augmentations", cc.xy(c, r));
		builder.add(new AssetTablePanel(augmentationTable), cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Items", cc.xy(c, r));
		builder.add(new AssetTablePanel(itemTable), cc.xy(c + 2, r));
		r += 2;
		
		aggressionBox.setSelectedItem(Aggression.UNAGGRESSIVE);
		builder.addLabel("Aggression", cc.xy(c, r));
		builder.add(aggressionBox, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Can Speak", cc.xy(c, r));
		builder.add(speakCheck, cc.xy(c + 2, r));
		r += 2;
		
		builder.addLabel("Dialogue Tree", cc.xy(c, r));
		builder.add(new AssetTablePanel(dialogueTable), cc.xy(c + 2, r));
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
		
		builder.addLabel("Traits", cc.xy(c, r));
		builder.add(new AssetTablePanel(traitTable), cc.xy(c + 2, r));
		r += 2;
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.add(saveButton, cc.xy(c + 4, r));
		
		if (prev.isPresent()) {
			/*
			Augmentation asset = prev.get();
			idField.setText(asset.getId());
			nameField.setText(asset.getName());
			descriptionField.setText(asset.getDescription());
			valueField.setText(asset.getValue() + "");
			typeBox.setSelectedItem(asset.getType());
			for (Effect effect : asset.getEffectList()) {
				effectTable.addAsset(effect);
			}
			for (Requirement req : asset.getRequirementList()) {
				requirementTable.addAsset(req);
			}
			*/
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(1300, 525));
	}

	@Override
	public NonPlayerActor createAsset() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private class SpeciesSelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean visible = true;
			Species s = (Species) speciesBox.getSelectedItem();
			switch (s) {
				case HUMAN:
				case UNDEAD:
					break;
				default:
					visible = false;
			}
			
			genderBox.setVisible(visible);
			professionBox.setVisible(visible);
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
}
