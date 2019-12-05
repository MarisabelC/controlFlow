import java.io.IOException;

public class TestControlFlowGraph {
	

	public static void main(String[] args) {
		try {
			ControlFlowGraph graph = new ControlFlowGraph(args[0]);
			graph.printNodes();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}


