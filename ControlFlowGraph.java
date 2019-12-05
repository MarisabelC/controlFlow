
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class ControlFlowGraph {
	private class Pair<K, V> {
		K key;
		V value;
		Node connection;
		boolean _continue;
		Node _break ;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Pair))
				return false;
			if (other == this)
				return true;
			@SuppressWarnings("unchecked")
			Pair<K, V> pair = (Pair<K, V>) other;
			return key.equals(pair.key) && value.equals(pair.value);
		}

		@Override
		public int hashCode() {
			return (int) value;
		}

		@Override
		public String toString() {
			return key.toString() + " - " + value.toString();
		}

	}

///////////////////////////////////////////////////////////////////////////////////////
	private Lexer lexer;
	private int id;
	private Node head;
	private Queue<String> tokenGroup;
	private boolean isOr, isAnd;
	private String token;
	private Node _continue;

	public ControlFlowGraph(String fileName) throws IOException {
		lexer = new Lexer(new FileReader(fileName));
		setTokenGroup();
		Pair<Node, List<Node>> nodes = getcurrentNode();
		head = nodes.key;
	}


	private void setTokenGroup() throws IOException {
		tokenGroup = new LinkedList<>();
		Yytoken token;
		while (!lexer.isZzAtEOF()) {
			token = lexer.yylex();
			if (token != null)
				tokenGroup.add(token.m_text);
		}
	}
		
	private Pair<Node, List<Node>> getcurrentNode() throws IOException {
		Node start = null, previousNode = null, currentNode = null, _break=null;
		Pair<Node, List<Node>> conditionList = null, nodes = null;
		String preCondition = "previous", condition = "condition";
		StringBuilder declaration = new StringBuilder();
		List<Node> sequence = null, end = new ArrayList<Node>();
		Pair<Node, List<Node>> node = new Pair<Node, List<Node>>(start, end);
		while (!tokenGroup.isEmpty()) {
			token = tokenGroup.remove();
			if (isLoop(token) || isIf(token) || isElse(token) || token.equals("do")) {
				if (currentNode != null) {
					previousNode = currentNode;
					currentNode = null;
				}
				preCondition = condition;
				condition = token;
				if (!condition.equals("do") && !isElse(condition)) {
					conditionList = conditionStatement(condition);
					sequence = conditionList.value;
					if (preCondition.equals("do")) 
						connectConditionToPrevious(previousNode, end, sequence);
					else if (previousNode == null) 
						start = conditionList.key;
					else {
						connectNode(previousNode, conditionList.key);
						connectEndToPreviousNode(end, conditionList.key);
					}
					if (conditionList != null && isAnd == true) {
						end.addAll(conditionList.value);
						isAnd = false;
					}
					previousNode = sequence.get(sequence.size() - 1);
				}
				if (condition.equals("do")) 
					previousNode = getStatement(new StringBuilder(token), currentNode, previousNode, 
							preCondition, condition, end);
				continue;
				
			} else if (token.equals("}")) {
				if (isIf(preCondition) && isElse(condition) && 
						isStatementEqualsLastElement(previousNode, sequence)) {
				} else if(node._break == null) {
					if (currentNode != null)
					end.add(currentNode);
					else
						end.add(previousNode);
				}
				if (_break != null)
					end.add(_break); 
				break;
				
			} else if (token.equals("{")) {
				if (currentNode != null) {
					previousNode = currentNode;
					currentNode = null;
				}
				nodes = getcurrentNode();
				if (isOr == true && conditionList != null) {
					connectNodeGroup(sequence, nodes.key);
					conditionList = null;
					isOr = false;
				} else
					connectNode(previousNode, nodes.key);
				if (nodes != null && nodes._continue == true)
					this._continue = nodes.value.get(0);
				else if (!isLoop(condition)) {
					end.addAll(nodes.value);
				}
				else if (condition.equals("for"))
					previousNode = conditionList.connection;
				if (isElse(condition)) {
					end.remove(previousNode);
					previousNode = nodes.value.get(0);
				}
				if (condition.equals("for") || isWhile(condition, preCondition)) {
					int last= nodes.value.size()-1;
					if (nodes.value.get(last).getStatements().equals("break ")) {
						_break= nodes.value.remove(last);
					}
					connectToLoopCondition(nodes.value, conditionList.connection);
					if (_break != null)
						end.add(_break);
				}
				_break=nodes._break;
				continue;
			}
			else if (isSemiColon(token) || tokenGroup.peek().equals("{")) {
				currentNode = getStatement(declaration, currentNode, previousNode, 
						preCondition, condition, end);
				if (declaration.toString().equals("continue "))
					node._continue = true;
				else if (declaration.toString().equals("break "))
					node._break = currentNode;
				if (start == null) {
					start = currentNode;
				}
				declaration = new StringBuilder();
			} else {
				declaration.append(token + " ");
			}

		}
		
		node.key=start;
		node.value=end;
		return node;
	}

	/******************************************************************/
	private Pair<Node, List<Node>> conditionStatement(String condition) throws IOException {
		List<Node> conditionalGroup = new ArrayList<>();
		StringBuilder expression = new StringBuilder(), wholeExpression = new StringBuilder();
		int semicolon = 0;
		boolean comparisson = false;
		String previousToken;
		Node previousNode = null, connection = null, start = null;

		while (!tokenGroup.peek().equals("{")) {
			previousToken = token;
			token = tokenGroup.poll();
			wholeExpression.append(token);
			if (isSemiColon(token) || isParenthesis(token) || isBooleanOperator(token)) {
				if (isBooleanOperator(token))
					wholeExpression = new StringBuilder();
				if (expression.length() != 0) {
					if (isComparissonOperator(previousToken)) {
						comparisson = true;
						expression = new StringBuilder();
						continue;
					}
					Node currentNode = new Node(++id, expression);
					if (previousNode != null) {
						connectNode(previousNode, currentNode);
						if (hasBooleanOperator())
							conditionalGroup.add(previousNode);
					} else {
						start = currentNode;
						if (isLoop(condition))
							connection = currentNode;
					}
					if (comparisson == true) {
						currentNode = connectNode(currentNode, ++id, wholeExpression);
						if (isLoop(condition) && !hasBooleanOperator())
							connection = currentNode;
					} else if (semicolon == 2)
						connection = previousNode;
					else if (token.equals("||"))
						isOr = true;
					else if (token.equals("&&"))
						isAnd = true;
					previousNode = currentNode;
					expression = new StringBuilder();
					if (isSemiColon(tokenGroup.peek()) && isWhile(condition))
						break;
				}
				if (isSemiColon(token))
					++semicolon;
				continue;
			} else
				expression.append(token);
		}
		conditionalGroup.add(previousNode);
		return setPairNode(start, conditionalGroup, connection);
	}

	/******************************************************************/
	private Node getStatement(StringBuilder declaration, Node currentNode, Node previousNode, String preCondition,
			String condition, List<Node> end) {
		if (declaration.length() != 0) {
			if (currentNode == null || declaration.equals("case "))
				currentNode = new Node(++id, declaration);
			else
				currentNode.addStatement(declaration);
			if (!preCondition.equals("else")) 
				connectNode(previousNode, currentNode);
			connectEndToPreviousNode(end, currentNode);
		}
		return currentNode;
	}
	
	/******************************************************************/
	private void connectEndToPreviousNode(List<Node> end, Node currentNode) {
		if (!end.isEmpty()) {
			connectNodeGroup(end, currentNode);
			end.clear();
		}	
	}

	/******************************************************************/
	private void connectToLoopCondition(List<Node> src, Node connection) {
		connectNodeGroup(src, connection);
		connectNode(_continue, connection);
		_continue = null;
	}

	/******************************************************************/
	private void connectConditionToPrevious(Node previousNode, List<Node> end, List<Node> conditionSequence) {
		if (!end.isEmpty()) {
			connectNodeGroup(end, conditionSequence.get(0));
			connectDoWhile(previousNode, conditionSequence);
			end.clear();
		} else {
			connectNode(previousNode, conditionSequence.get(0));
		}
	}

	/******************************************************************/
	private void connectNodeGroup(List<Node> src, Node dest) {
		if (src != null)
			for (Node node : src)
				connectNode(node, dest);
	}
	

	/******************************************************************/
	private Node connectNode(Node currentNode, int id, StringBuilder statement) {
		Node temp = new Node(id, statement);
		connectNode(currentNode, temp);
		return temp;

	}

	/******************************************************************/
	private void connectNode(Node curr, Node next) {
		if (curr == null || next == null)
			return;
		curr.setChild(next);
//		System.out.println(curr + "=>" + next);

	}

	/******************************************************************/
	private void connectDoWhile(Node _do, List<Node> conditionGroup) {
		for (Node condition : conditionGroup) {
			connectNode(condition, _do);
		}
	}

	/******************************************************************/
	private Pair<Node, List<Node>> setPairNode(Node start, List<Node> conditionalGroup, Node connection) {
		Pair<Node, List<Node>> nodes = new Pair<Node, List<Node>>(start, conditionalGroup);
		nodes.connection = connection;
		return nodes;
	}

	/********************************************************************/
	private boolean hasBooleanOperator() {
		return isAnd == true || isOr == true;
	}

	private boolean isStatementEqualsLastElement(Node statement, List<Node> sequence) {
		int lastElement = sequence.size() - 1;
		return statement.equals(sequence.get(lastElement));
	}

	/********************************************************************/
	private boolean isElse(String condition) {
		return condition.equals("else");
	}

	/********************************************************************/
	private boolean isIf(String condition) {
		return condition.equals("if");
	}

	/********************************************************************/
	private boolean isWhile(String condition) {
		return condition.equals("while");
	}

	/********************************************************************/
	private boolean isSemiColon(String token) {
		return token.equals(";");
	}

	/********************************************************************/
	private boolean isLoop(String condition) {
		return condition.equals("for") || condition.equals("while");
	}

	/********************************************************************/
	private boolean isWhile(String condition, String preCondition) {
		return !preCondition.equals("do") && condition.equals("while");
	}

	/********************************************************************/
	private boolean isBooleanOperator(String token) {
		String[] booleanOperator = { "&&", "||" };
		for (String operator : booleanOperator)
			if (operator.equals(token)) {
				return true;
			}
		return false;
	}

	/********************************************************************/
	private boolean isParenthesis(String token) {
		String[] parentheses = { "(", ")" };
		for (String parenthesis : parentheses)
			if (parenthesis.equals(token)) {
				return true;
			}
		return false;
	}

	/******************************************************************/
	private boolean isComparissonOperator(String token) {
		String[] comparissonOperator = { "==", "!=", "<=", ">=", "<", ">" };
		for (String operator : comparissonOperator)
			if (operator.equals(token)) {
				return true;
			}
		return false;
	}

	/******************************************************************/
	public void printNodes() throws IOException {
	    BufferedWriter writer = new BufferedWriter(new FileWriter("graph.dot", false));
	    writer.write("digraph {\n");
		Set<Pair<Integer, Integer>> set = new HashSet<>();
		Set<String> vertices = new TreeSet<>();
		List<String> edges = new ArrayList<>();
		printNodes(head, set, vertices, edges);
		System.out.println("Vertices: \n");
		for (String vertex : vertices)
			System.out.println(vertex);
		System.out.println("\n=================================");
//		System.out.println("\nEdges: \n");
		for (String edge : edges) {
//			System.out.println(edge);
			writer.append(edge+"\n");
		}
		writer.append("}");
		writer.close();
	}

	/******************************************************************/
	public void printNodes(Node head, Set<Pair<Integer, Integer>> set, Set<String> vertices,
			List<String> edges) {
		if (head != null) {
			vertices.add(head.toString());
			for (Node child : head.getChildren()) {
				Pair<Integer, Integer> pair = new Pair<Integer, Integer>(head.getId(), child.getId());
				StringBuilder str = new StringBuilder();
				if (!set.contains(pair)) {
					str.append(head.getId());
					str.append("  ->  ");
					str.append(child.getId()+";");
					edges.add(str.toString());
					set.add(pair);
					printNodes(child, set, vertices, edges);
				}
			}
		}
	}
	

}
