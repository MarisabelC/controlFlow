import java.io.IOException;

public class TestControlFlowGraph {
	

	public static void main(String[] args) {
		try {
			ControlFlowGraph graph = new ControlFlowGraph(args[0]);
			graph.printNodes();
//			Control_Flow_Gui gui= new  Control_Flow_Gui(graph);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}


