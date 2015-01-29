package com.eldritch.scifirpg.editor.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.text.WordUtils;

import com.eldritch.scifirpg.editor.tables.AssetTable;
import com.google.common.base.Optional;
import com.google.protobuf.Message;

public abstract class AssetEditorPanel<T extends Message, S extends AssetTable<T>> extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final S table;
	private final JFrame frame;
	private Optional<T> prev;

	public AssetEditorPanel(S table, JFrame frame, Optional<T> prev) {
		super(new BorderLayout());
		this.table = table;
		this.frame = frame;
		this.prev = prev;
	}
	
	protected final JTextArea createArea(boolean lineWrap, int columns, Dimension minimumSize) {
		JTextArea area = new JTextArea();
		area.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
				new EmptyBorder(1, 3, 1, 1)));
		area.setLineWrap(lineWrap);
		area.setWrapStyleWord(true);
		area.setColumns(columns);
		if (minimumSize != null) {
			area.setMinimumSize(new Dimension(100, 32));
		}
		return area;
	}
	
	public S getTable() {
		return table;
	}

	public JFrame getFrame() {
		return frame;
	}
	
	public void setPrev(T value) {
		prev = Optional.of(value);
	}

	public Optional<T> getPrev() {
		return prev;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		save();
	}
	
	protected void save() {
		T asset = createAsset();
		table.saveAsset(prev, asset);
		frame.dispose();
	}
	
	public abstract T createAsset();
	
	protected static class NameTypedListener implements ActionListener {
		private final JTextField idField;
		
		public NameTypedListener(JTextField idField) {
			this.idField = idField;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField source = (JTextField) e.getSource();
			idField.setText(WordUtils.capitalizeFully(source.getText()).replaceAll(" ", ""));
		}
	}
}
