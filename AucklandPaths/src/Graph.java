import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import javax.xml.crypto.NodeSetData;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 *
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	Node highlightedNode;
	Collection<Road> highlightedRoads = new HashSet<>();
	Collection<Segment> highlightedSegs = new HashSet<>();
	Collection<Node> routeNodes = new HashSet<>();
	Collection<Node> artPoints = new HashSet<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(Mapper.SEGMENT_COLOUR);
		for (Segment s : segments)
			s.draw(g2, origin, scale);


		for (Segment s: highlightedSegs){
			//System.out.println("YAS DRAWING");
			g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.GREEN);
			s.draw(g2, origin, scale);
		}

		// draw the segments of all highlighted roads.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}
		
		for (Node n: routeNodes){
			g2.setColor(Color.pink);
			n.draw(g2,screen, origin, scale);
		}

		for(Node n: artPoints){
			g2.setColor(Color.white);
			n.draw(g2, screen, origin, scale);
		}

	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	void setHighlightS(Collection<Segment> segs){
		this.highlightedSegs = segs;
	} //Added
	
	void setHighlightN(Collection<Node> routeNodes) {
		this.routeNodes = routeNodes;
	} //Added
}

// code for COMP261 assignments