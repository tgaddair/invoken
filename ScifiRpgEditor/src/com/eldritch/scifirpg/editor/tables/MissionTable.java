package com.eldritch.scifirpg.editor.tables;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.MissionEditorPanel;
import com.eldritch.invoken.proto.Missions.Mission;
import com.google.common.base.Optional;
import com.google.protobuf.TextFormat;

public class MissionTable extends MajorAssetTable<Mission> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name" };
	
	public MissionTable() {
		super(COLUMN_NAMES, "Mission");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Mission> prev, JFrame frame) {
		return new MissionEditorPanel(this, frame, prev);
	}

	@Override
	protected Object[] getDisplayFields(Mission asset) {
		return new Object[]{asset.getId(), asset.getName()};
	}

	@Override
	protected String getAssetDirectory() {
		return "missions";
	}

	@Override
	protected String getAssetId(Mission asset) {
		return asset.getId();
	}

	@Override
	protected Mission readFromBinary(InputStream is) throws IOException {
		return Mission.parseFrom(is);
	}
	
	@Override
	protected Mission readFromText(InputStream is) throws IOException {
		Mission.Builder builder = Mission.newBuilder();
		TextFormat.merge(new InputStreamReader(is), builder);
		return builder.build();
	}
}
