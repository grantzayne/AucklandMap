
public class Fringe implements Comparable<Object> {
	Node current;
	Node prev;
	private double costToHere;
	private double totalCost;
	
	Fringe(Node node, Node prev, double cost, double total){
		this.current = node;
		this.prev = prev;
		this.costToHere = cost;
		this.totalCost = total;
	}

	public int compareTo(Object other){
		Fringe f = (Fringe) other;
		return Double.compare(this.totalCost, f.totalCost);
	}
	
	Node getCurrent(){
		return current;
	}
	Node getPrev(){
		return prev;
	}
	double getCost(){
		return costToHere;
	}
	public String toString(){
		return ("Current: "+current+" Prev: "+prev+" Cost: "+costToHere+" TotalCost: "+totalCost);
	}
}
