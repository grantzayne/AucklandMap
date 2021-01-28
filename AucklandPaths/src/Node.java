import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 *
 */
public class Node implements Comparable<Node>{

	private final int nodeID;
	final Location location;
	private final Collection<Segment> segments;
	private boolean visited;
	private double heuristic;
	private List<Segment> adj = new ArrayList<>();
	Node best;
	private List<Segment> outN = new ArrayList<>();
	private List<Segment> inN = new ArrayList<>();


	Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<>();
	}

	void addSegment(Segment seg) {
		segments.add(seg);
	}

	void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}
	
	void setVisit(boolean visit){
		this.visited = visit;
	}

	boolean isVisited(){
		return this.visited;
	}

	void setHeuristic(double heur){
		this.heuristic = heur;
	}

	double getHeuristic(){
		return this.heuristic;
	}

	void addToAdj(Segment s){
		this.adj.add(s);
	}

	Location getLocation(){
		return location;
	}

	ArrayList<Node> getNeighbours() {
		ArrayList<Node> neighbours = new ArrayList<>();
		for (Segment s:getOutNeighbours()){
			neighbours.add(s.getNeighbours(this));
		}
		return neighbours;
	}

	void addInSegment(Segment s){
		inN.add(s);
	}	
	void addOutSegment(Segment s){
		outN.add(s);
	}	

	private List<Segment> getOutNeighbours(){
		return outN;
	}

	private double costToNode(){
		return 0;
	}


	Segment getBetween(Node other) {
		for (Segment s : this.adj) {
			for (Segment os : other.adj) {
				if (s.equals(os))
					return s;
			}
		}
		return null;
	}


	public int compareTo(Node other) {
		return (int) ((this.costToNode()+this.getHeuristic()) - (other.costToNode()+other.getHeuristic()));
	}

	public String toString() {
		Set<String> edges = new HashSet<>();
		for (Segment s : segments) {
			edges.add(s.road.name);
		}

		StringBuilder strBuilder = new StringBuilder("ID: " + nodeID + "  loc: " + location + "\nroads: ");
		for (String e : edges) {
			strBuilder.append(e).append(", ");
		}
		String str = strBuilder.toString();
		return str.substring(0, str.length() - 2);
	}


}

// code for COMP261 assignments