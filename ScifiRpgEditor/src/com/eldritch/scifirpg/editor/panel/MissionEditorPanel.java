package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.eldritch.scifirpg.editor.tables.MissionTable;
import com.eldritch.scifirpg.proto.Missions.Mission;
import com.eldritch.scifirpg.proto.Missions.Mission.Stage;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class MissionEditorPanel extends AssetEditorPanel<Mission, MissionTable> {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextField nameField = new JTextField();
	private final StageTable stageTable = new StageTable();

	public MissionEditorPanel(MissionTable owner, JFrame frame, Optional<Mission> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		nameField.addActionListener(new NameTypedListener(idField));
		builder.append("Name:", nameField);
		builder.nextLine();
		
		builder.append("ID:", idField);
		builder.nextLine();
		
		builder.appendRow("fill:p:grow");
		builder.append("Stages:", new AssetTablePanel(stageTable));
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Mission asset = prev.get();
			idField.setText(asset.getId());
			nameField.setText(asset.getName());
			for (Stage rank : asset.getStageList()) {
				stageTable.addAsset(rank);
			}
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(650, 750));
	}

	@Override
	public Mission createAsset() {
		String id = idField.getText();
		String name = nameField.getText();
		return Mission.newBuilder()
				.setId(id)
				.setName(name)
				.addAllStage(stageTable.getSortedAssets())
				.build();
	}
	
	private static class StageTable extends AssetTable<Stage> {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUMN_NAMES = { 
			"ID", "Description", "Finished" };
		
		public StageTable() {
			super(COLUMN_NAMES, "Stage");
		}

		@Override
		protected JPanel getEditorPanel(Optional<Stage> prev, JFrame frame) {
			return new StageEditorPanel(this, frame, prev);
		}
		
		@Override
		protected Object[] getDisplayFields(Stage asset) {
			Object finished = asset.getFinished() ? "yes" : "";
			return new Object[]{asset.getId(), asset.getDescription(), finished};
		}
		
		public List<Stage> getSortedAssets() {
			List<Stage> assets = new ArrayList<>(getAssets());
			Collections.sort(assets, new Comparator<Stage>() {
				@Override
				public int compare(Stage a1, Stage a2) {
					return Integer.compare(a1.getId(), a2.getId());
				}
			});
			return assets;
		}
	}
	
	private static class StageEditorPanel extends AssetEditorPanel<Stage, StageTable> {
		private static final long serialVersionUID = 1L;

		private final JTextField idField = new JTextField();
		private final JTextArea descriptionField = createArea(true, 30, new Dimension(100, 100));
		private final JCheckBox finishedCheck = new JCheckBox();

		public StageEditorPanel(StageTable owner, JFrame frame, Optional<Stage> prev) {
			super(owner, frame, prev);

			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
			builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			builder.appendColumn("right:pref");
			builder.appendColumn("3dlu");
			builder.appendColumn("fill:max(pref; 100px)");
			
			builder.append("ID:", idField);
			builder.nextLine();

			builder.append("Description:", descriptionField);
			builder.nextLine();
			
			builder.append("Finished:", finishedCheck);
			builder.nextLine();

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			builder.append(saveButton);
			builder.nextLine();
			
			if (prev.isPresent()) {
				Stage asset = prev.get();
				idField.setText(asset.getId() + "");
				descriptionField.setText(asset.getDescription());
				finishedCheck.setSelected(asset.getFinished());
			}

			add(builder.getPanel());
			setPreferredSize(new Dimension(500, 500));
		}

		@Override
		public Stage createAsset() {
			return Stage.newBuilder()
					.setId(Integer.parseInt(idField.getText()))
					.setDescription(descriptionField.getText())
					.setFinished(finishedCheck.isSelected())
					.build();
		}
	}
}
