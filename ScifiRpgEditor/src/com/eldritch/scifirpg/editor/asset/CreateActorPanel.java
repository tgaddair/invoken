package com.eldritch.scifirpg.editor.asset;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.*; //for action events
import java.util.ArrayList;
import java.util.List;

public class CreateActorPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private String newline = "\n";
	protected static final String idString = "ID";
	protected static final String passwordFieldString = "JPasswordField";
	protected static final String ftfString = "JFormattedTextField";
	protected static final String buttonString = "JButton";

	protected JLabel actionLabel;
	
	private void addTo(List<JTextField> textFields, List<JLabel> labels, String field) {
		JTextField textField = new JTextField(10);
		textField.setActionCommand(field);
		textField.addActionListener(this);
		
		JLabel label = new JLabel(field + ": ");
		label.setLabelFor(textField);
		
		textFields.add(textField);
		labels.add(label);
	}

	public CreateActorPanel() {
		setLayout(new BorderLayout());
		
		List<JTextField> fieldList = new ArrayList<>();
		List<JLabel> labelList = new ArrayList<>();

		addTo(fieldList, labelList, "ID");
		addTo(fieldList, labelList, "Name");
		
		// Create a label to put messages during an action event.
		actionLabel = new JLabel("Type text in a field and press Enter.");
		actionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		// Lay out the text controls and the labels.
		JPanel textControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		textControlsPane.setLayout(gridbag);

		JLabel[] labels = labelList.toArray(new JLabel[0]);
		JTextField[] textFields = fieldList.toArray(new JTextField[0]);
		addLabelTextRows(labels, textFields, gridbag, textControlsPane);

		c.gridwidth = GridBagConstraints.REMAINDER; // last
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		textControlsPane.add(actionLabel, c);
		textControlsPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Text Fields"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// Create a text area.
		JTextArea textArea = new JTextArea("This is an editable JTextArea. "
				+ "A text area is a \"plain\" text component, "
				+ "which means that although it can display text "
				+ "in any font, all of the text is in the same font.");
		textArea.setFont(new Font("Serif", Font.ITALIC, 16));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Plain Text"),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)),
				areaScrollPane.getBorder()));

		// Create an editor pane.
		JEditorPane editorPane = createEditorPane();
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));

		// Create a text pane.
		JTextPane textPane = createTextPane();
		JScrollPane paneScrollPane = new JScrollPane(textPane);
		paneScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		paneScrollPane.setPreferredSize(new Dimension(250, 155));
		paneScrollPane.setMinimumSize(new Dimension(10, 10));

		// Put the editor pane and the text pane in a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				editorScrollPane, paneScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		JPanel rightPane = new JPanel(new GridLayout(1, 0));
		rightPane.add(splitPane);
		rightPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Styled Text"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// Put everything together.
		JPanel leftPane = new JPanel(new BorderLayout());
		leftPane.add(textControlsPane, BorderLayout.PAGE_START);
		leftPane.add(areaScrollPane, BorderLayout.CENTER);

		add(leftPane, BorderLayout.LINE_START);
		add(rightPane, BorderLayout.LINE_END);
	}

	private void addLabelTextRows(JLabel[] labels, JTextField[] textFields,
			GridBagLayout gridbag, Container container) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		int numLabels = labels.length;

		for (int i = 0; i < numLabels; i++) {
			c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
			c.fill = GridBagConstraints.NONE; // reset to default
			c.weightx = 0.0; // reset to default
			container.add(labels[i], c);

			c.gridwidth = GridBagConstraints.REMAINDER; // end row
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			container.add(textFields[i], c);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String prefix = "You typed \"";
		if (idString.equals(e.getActionCommand())) {
			JTextField source = (JTextField) e.getSource();
			actionLabel.setText(prefix + source.getText() + "\"");
		} else if (passwordFieldString.equals(e.getActionCommand())) {
			JPasswordField source = (JPasswordField) e.getSource();
			actionLabel.setText(prefix + new String(source.getPassword())
					+ "\"");
		} else if (buttonString.equals(e.getActionCommand())) {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private JEditorPane createEditorPane() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		return editorPane;
	}

	private JTextPane createTextPane() {
		String[] initString = {
				"This is an editable JTextPane, ", // regular
				"another ", // italic
				"styled ", // bold
				"text ", // small
				"component, ", // large
				"which supports embedded components..." + newline,// regular
				" " + newline, // button
				"...and embedded icons..." + newline, // regular
				" ", // icon
				newline
						+ "JTextPane is a subclass of JEditorPane that "
						+ "uses a StyledEditorKit and StyledDocument, and provides "
						+ "cover methods for interacting with those objects." };

		String[] initStyles = { "regular", "italic", "bold", "small", "large",
				"regular", "button", "regular", "icon", "regular" };

		JTextPane textPane = new JTextPane();
		StyledDocument doc = textPane.getStyledDocument();
		addStylesToDocument(doc);

		try {
			for (int i = 0; i < initString.length; i++) {
				doc.insertString(doc.getLength(), initString[i],
						doc.getStyle(initStyles[i]));
			}
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}

		return textPane;
	}

	protected void addStylesToDocument(StyledDocument doc) {
		// Initialize some styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);

		s = doc.addStyle("bold", regular);
		StyleConstants.setBold(s, true);

		s = doc.addStyle("small", regular);
		StyleConstants.setFontSize(s, 10);

		s = doc.addStyle("large", regular);
		StyleConstants.setFontSize(s, 16);

		s = doc.addStyle("icon", regular);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
		ImageIcon pigIcon = createImageIcon("images/Pig.gif", "a cute pig");
		if (pigIcon != null) {
			StyleConstants.setIcon(s, pigIcon);
		}

		s = doc.addStyle("button", regular);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
		ImageIcon soundIcon = createImageIcon("images/sound.gif", "sound icon");
		JButton button = new JButton();
		if (soundIcon != null) {
			button.setIcon(soundIcon);
		} else {
			button.setText("BEEP");
		}
		button.setCursor(Cursor.getDefaultCursor());
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setActionCommand(buttonString);
		button.addActionListener(this);
		StyleConstants.setComponent(s, button);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path, String description) {
		return null;
	}
}
