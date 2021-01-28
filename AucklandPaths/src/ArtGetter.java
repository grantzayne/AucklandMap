public class ArtGetter {

	private Node node;
	private Node parent;
	public int count = 0;

	public ArtGetter(Node n, int c, Node p){
		this.node = n;
		this.count = c;
		this.parent = p;
	}

	public Node getNode(){
		return this.node;
	}

	public Node getParent(){
		return this.parent;
	}

}
