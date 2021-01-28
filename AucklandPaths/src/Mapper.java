import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 *
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;

	// My personal lists for various methods
	private List <Node> clickedNodes = new ArrayList <>();
	private List <Node> pathNodes = new ArrayList<>();
	private Set <Road> toHighlight = new HashSet<>();
	private List<Segment> selSeg = new ArrayList<>();
	private double finalCost=0;
	private NumberFormat formatter = new DecimalFormat("#0.00"); // round to 2dp
    private  boolean pathFind = false;
	private boolean showArticulation = false;
	private Set<Node> articulationPoints;
	private double SubTrees = 0;
	private double neighbours = 0;

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		toHighlight.clear();
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// If it's close enough, highlight it and show some information.
		if (closest != null && clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}

		/*
		 * Calls the Articulation Points method. but failed to create method
		 */
        if(showArticulation){
            Node root = closest;
            System.out.print("FINDING ART POINTS");
            for(Node neigh : root.getNeighbours()){
                neighbours++;
                if(neighbours == Double.POSITIVE_INFINITY){
                    articulationPoints(neigh,1,root);
                    SubTrees++;
                }
                if(SubTrees > 1){
                    articulationPoints.add(root);
                }
            }
            System.out.print("FOUND ART POINTS");
        }
        if(pathFind) {
            if (clickedNodes.size() == 0) {//if there are no clickedNodes, add closest
                clickedNodes.add(closest);
            } else if (clickedNodes.size() == 1) {//if there is one already, add another and find path
                clickedNodes.add(closest);
                pathfinder(clickedNodes.get(0), clickedNodes.get(1));
                graph.highlightedSegs = selSeg;
                graph.setHighlightS(selSeg);
                graph.setHighlightN(clickedNodes);
                redraw();
                List<String> roadNames = new ArrayList<>();
                for (Segment s : selSeg) {
                    roadNames.add(s.road.name + " " + formatter.format(s.length) + "km ->");
                }
                Collections.reverse(roadNames);
                getTextOutputArea().setText("Distance: " + formatter.format(finalCost) + "km START " + roadNames + " END");
            } else if (clickedNodes.size() == 2) {//if there are two, then clear the array and start adding again
                clickedNodes.clear();
                clickedNodes.add(closest);
            }
        }
	}



	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		StringBuilder str = new StringBuilder();
		for (String name : names)
			str.append(name).append("; ");

		if (str.length() != 0)
			str = new StringBuilder(str.substring(0, str.length() - 2));
		getTextOutputArea().setText(str.toString());
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	/**
	 * A* Algorithm
	 * Finds the most cost effective path to the goal
	 */
	private void pathfinder(Node start, Node goal){
	    if(pathFind) {
            pathNodes.add(start);
            finalCost = Double.POSITIVE_INFINITY;
            for (Node n : graph.nodes.values()) { //Initialises all the nodes
                n.setVisit(false);
                n.setHeuristic(Double.POSITIVE_INFINITY);
            }
            Fringe startF = new Fringe(start, null, 0, estimate(start, goal));
            PriorityQueue<Fringe> Fringe = new PriorityQueue<>(); //start a new Fringe
            Fringe.offer(startF);//add the first Fringe
            while (!Fringe.isEmpty()) {
                Fringe current = Fringe.poll();
                Node node = current.getCurrent();
                Node prev = current.getPrev();
                double costToHere = current.getCost();

                if (costToHere < node.getHeuristic()) {//if the cost is less than the dist(better route)
                    node.best = prev;
                    node.setHeuristic(costToHere);
                    if (!node.isVisited()) {
                        node.setVisit(true);
                        node.setHeuristic(costToHere);
                    }
                    if (node == goal) {
                        finalCost = costToHere;
                        returnPath(current);
                    }
                }
                for (Node neighbour : node.getNeighbours()) { //Ensures the algorithm goes through all neighbours
                    double costToN = costToHere + node.getBetween(neighbour).length;
                    if (costToN < neighbour.getHeuristic()) {
                        double estTotal = costToN + estimate(neighbour, goal);
                        if (estTotal < finalCost) {
                            Fringe newF = new Fringe(neighbour, node, costToN, estTotal);
                            Fringe.offer(newF);
                            returnPath(newF);
                        }
                        if (neighbour == goal) {
                            finalCost = costToN;
                        }
                    }
                }
            }
        }
	}

    /**
     * Gets the seg to be highlighted as the path and moves nodes along so we can start the next search
     *
     */
	private void returnPath(Fringe current) {
		selSeg = new ArrayList<>();
		Node goal = current.current;
		Node start = current.prev;
		while(start!=null){
			selSeg.add(goal.getBetween(start));//get the segments in between by iterating through all the neighbouring segments
			Node temp = start;
			start = start.best;
			goal = temp;
		}
	}

    /**
     * Give the estimation/absolute total cost of the path
     */
	private double estimate(Node start, Node goal){
		return Math.abs((start.getLocation().x - goal.getLocation().x)+Math.abs(start.getLocation().y-goal.getLocation().y));

	}

    protected void togglePath() {
        this.pathFind = !this.pathFind;
        clickedNodes.clear();
        pathNodes.clear();
        toHighlight.clear();
        selSeg.clear();
        redraw();
    }

    protected void toggleArticulation() {
		this.showArticulation  = !this.showArticulation;
	}

	public void articulationPoints(Node node, int count, Node parent){
	    neighbours = 0;
	    double reachBack = count;
	    for(Node neigh : node.getNeighbours()){
	        neighbours++;
	        if(neighbours < Double.POSITIVE_INFINITY){

            }
        }
	}


    public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments