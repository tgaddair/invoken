package com.eldritch.scifirpg.editor.asset;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.google.common.base.Optional;
import com.google.protobuf.Message;

public abstract class AssetEditorPanel<T extends Message, S extends AssetTable<T>> extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final S table;
	private final JFrame frame;
	private final Optional<T> prev;

	public AssetEditorPanel(S table, JFrame frame, Optional<T> prev) {
		super(new BorderLayout());
		this.table = table;
		this.frame = frame;
		this.prev = prev;
	}
	
	public S getTable() {
		return table;
	}

	public JFrame getFrame() {
		return frame;
	}

	public Optional<T> getPrev() {
		return prev;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		T asset = createAsset();
		table.saveAsset(prev, asset);
		frame.dispose();
	}
	
	public abstract T createAsset();
}
