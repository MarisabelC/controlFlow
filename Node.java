import java.util.HashSet;
import java.util.Set;

public class Node {
	private int id;
	private StringBuilder statement;
	private Set<Node> children;

	public Node(int id,StringBuilder statement) {
		this.id = id;
		this.statement = new StringBuilder(statement);
		children = new HashSet<>();
	}
	
	public void setChild(Node node) {
		children.add(node);
	}
	
	public void addStatement(StringBuilder statement) {
		this.statement.append("\n      " + statement);
	}
	
	public Set<Node> getChildren() {
		return children;
	}
	
	public String getStatements() {
		return statement.toString();
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) 
			return true;
		if (!(other instanceof Node))
			return false;
		Node node = (Node) other;
		return node.id == this.id;
	}
	
	@Override 
	public int hashCode() {
		return id;
	}
	
	@Override
	public String toString() {
		if (id<=9)
			return " (0" + Integer.toString(id)+") " + statement.toString();
		return " (" + Integer.toString(id)+") " + statement.toString();
	}

}
