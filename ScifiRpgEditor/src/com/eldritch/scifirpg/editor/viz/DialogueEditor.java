package com.eldritch.scifirpg.editor.viz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.scifirpg.editor.panel.DialogueEditorPanel.InfoClickListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class DialogueEditor extends Display {
	private static final long serialVersionUID = 3309144726299972847L;
	
	public enum NodeType {
		Greeting, Response, Choice
	}
	
	private final Map<String, Response> responses = new HashMap<>();
	private final Map<String, Choice> choices = new HashMap<>();

	public DialogueEditor(List<Response> dialogue, InfoClickListener clickListener) {
		super(new Visualization());

		Graph graph = makeGraph(dialogue);
		m_vis.addGraph("graph", graph);
		m_vis.setInteractive("graph.edges", null, false);
		m_vis.setValue("graph.nodes", null, VisualItem.SHAPE, new Integer(
				Constants.SHAPE_ELLIPSE));

		// draw the "name" label for NodeItems
		LabelRenderer nodeR = new LabelRenderer("summary");
		nodeR.setRoundedCorner(8, 8); // round the corners
//		Renderer nodeR = new ShapeRenderer(20);
		
		EdgeRenderer edgeR = new EdgeRenderer(
				prefuse.Constants.EDGE_TYPE_CURVE,
				prefuse.Constants.EDGE_ARROW_FORWARD);

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		DefaultRendererFactory drf = new DefaultRendererFactory();
		drf.setDefaultRenderer(nodeR);
		drf.setDefaultEdgeRenderer(edgeR);
		m_vis.setRendererFactory(drf);

		ColorAction nStroke = new ColorAction("graph.nodes",
				VisualItem.STROKECOLOR);
		nStroke.setDefaultColor(ColorLib.gray(100));

		int[] palette = new int[] { 
				ColorLib.rgb(170, 240, 209), 
				ColorLib.rgb(190, 190, 255),
				ColorLib.rgb(255, 180, 180) };
		DataColorAction nFill = new DataColorAction("graph.nodes", "type",
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		
		// use black for node text
		ColorAction text = new ColorAction("graph.nodes",
		    VisualItem.TEXTCOLOR, ColorLib.gray(0));
		ColorAction edges = new ColorAction("graph.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));
		ColorAction arrow = new ColorAction("graph.edges",
				VisualItem.FILLCOLOR, ColorLib.gray(200));
		ActionList color = new ActionList();
		color.add(nStroke);
		color.add(nFill);
		color.add(text);
		color.add(edges);
		color.add(arrow);

		ActionList layout = new ActionList(Activity.INFINITY);
//		layout.add(new ForceDirectedLayout("graph"));
		layout.add(new RepaintAction());
		
		ActionList formatter = new ActionList();
//		formatter.add(new BalloonTreeLayout("graph", 10));
//		formatter.add(new SquarifiedTreeMapLayout("graph", 10));
//		formatter.add(new RadialTreeLayout("graph", 250));
		formatter.add(new NodeLinkTreeLayout("graph", Constants.ORIENT_TOP_BOTTOM, 50, 50, 50));

		m_vis.putAction("color", color);
		m_vis.putAction("formatter", formatter);
		m_vis.putAction("layout", layout);

//		setSize(1200, 500); // set display size
		pan(360, 250);
		setHighQuality(true);
		addControlListener(clickListener);
		addControlListener(new DragControl());
		addControlListener(new PanControl());
		addControlListener(new ZoomControl()); // right mouse down and drag

		m_vis.run("color");
		m_vis.run("formatter");
		m_vis.run("layout");
	}
	
	public Response getResponse(String id) {
		return responses.get(id);
	}
	
	public Choice getChoice(String id) {
		return choices.get(id);
	}
	
	private Graph makeGraph(List<Response> dialogue) {
		// Create tables for node and edge data, and configure their columns.
		Table nodeData = new Table();
		nodeData.addColumn("id", String.class);
		nodeData.addColumn("summary", String.class);
		nodeData.addColumn("text", String.class);
		nodeData.addColumn("type", NodeType.class);
		
		Table edgeData = new Table(0, 1);
		edgeData.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
		edgeData.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);
		edgeData.addColumn("label", String.class);
		// Need more data in your nodes or edges? Just add more
		// columns.

		// Create Graph backed by those tables. Note that I'm
		// creating a directed graph here also.
		Graph g = new Graph(nodeData, edgeData, true);

		// Create the nodes in the graph, which are response and choices
		System.out.println("build graph");
		Map<String, Node> responseNodes = new HashMap<>();
		for (Response response : dialogue) {
			Node node = g.addNode();
			node.setString("id", response.getId());
			node.setString("summary", truncate(response.getText()));
			node.setString("text", response.getText());
			node.set("type", response.getGreeting() ? NodeType.Greeting : NodeType.Response);
			responseNodes.put(response.getId(), node);
			responses.put(response.getId(), response);
			System.out.println("response: " + response.getId());
		}
		
		for (Response response : dialogue) {
			int i = 0;
			for (Choice choice : response.getChoiceList()) {
				Node node = g.addNode();
				String choiceId = response.getId() + i++;
				node.setString("id", choiceId);
				node.setString("summary", truncate(choice.getText()));
				node.setString("text", choice.getText());
				node.set("type", NodeType.Choice);
				choices.put(choiceId, choice);
				
				// Add an edge from the response to this choice.
				Node parent = responseNodes.get(response.getId());
				Edge edge = g.addEdge(parent, node);
				
				for (String id : choice.getSuccessorIdList()) {
					// Add an edge from this choice to the successor.
					if (responseNodes.containsKey(id)) {
						Node next = responseNodes.get(id);
						Edge edgeNext = g.addEdge(node, next);
					}
				}
			}
		}
		
		return g;
	}
	
	private String truncate(String in) {
		int end = in.length();
		String suffix = "";
		int spaces = 0;
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i) == ' ') {
				spaces++;
			}
			if (spaces == 5) {
				end = i;
				suffix = "...";
				break;
			}
		}
		return in.substring(0, end) + suffix;
	}
}