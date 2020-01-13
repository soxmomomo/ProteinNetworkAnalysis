package logic;

import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 * @author Hanyi.Mo
 *
 * Network.java
 */

public class Network {
	private Set<Node> nodes;
	private Set<Edge> edges;
	private Map<Node, Integer> fullDistribution = new HashMap<Node, Integer>();

	public Network() {
		this.nodes = new HashSet<Node>();
		this.edges = new HashSet<Edge>();
	}

	/**
	 * To create a network from a file
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void createNetworkFromFile(String filename) throws IOException {
		Path file = Paths.get(filename);
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] twoNodes = line.split("\t");
				for (String str : twoNodes) {
					nodes.add(new Node(str));
				}
				Edge tmp = new Edge(new Node(twoNodes[0]), new Node(twoNodes[1]));
				addDegreesToMap(tmp, new Node[] { new Node(twoNodes[0]), new Node(twoNodes[1]) });
				edges.add(tmp);
			}
			reader.close();
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * To add an edge to the current network
	 * 
	 * @param name1
	 * @param name2
	 * @return
	 * @throws NullPointerException
	 */
	public String addInteraction(String name1, String name2) throws NullPointerException {
		if (name1 == null || name2 == null || name1.equals("") || name2.equals(""))
			throw new NullPointerException();
		Node node1 = new Node(name1);
		Node node2 = new Node(name2);
		Edge edge = new Edge(node1, node2);
		if (edges.contains(edge)) {
			return String.format("Interaction between %s and %s already exists!\n", node1.getName(), node2.getName());
		}
		addDegreesToMap(edge, new Node[] { node1, node2 });
		edges.add(edge);
		nodes.add(node1);
		nodes.add(node2);
		return String.format("Interaction between %s and %s added successfully!\n", node1.getName(), node2.getName());
	}

	/**
	 * To update the correspondent degree to nodes when the network initialised or
	 * the edge added
	 * 
	 * @param edge
	 * @param twoNodes
	 */
	public void addDegreesToMap(Edge edge, Node[] twoNodes) {
		if (!edges.contains(edge)) {
			for (Node node : twoNodes) {
				fullDistribution.put(node, fullDistribution.containsKey(node) ? fullDistribution.get(node) + 1 : 1);
			}
		}
	}

	/**
	 * To get degree for one node
	 * 
	 * @param node
	 * @return
	 */
	public int degreeOfNode(Node node) {
		if (!nodes.contains(node))
			return 0;
		return fullDistribution.get(node);
	}

	/**
	 * To get the average degree for the whole network
	 * 
	 * @return
	 */
	public double averageDegree() {
		int nodeSize = fullDistribution.size();
		int sum = 0;
		for (Node node : nodes) {
			sum += degreeOfNode(node);
		}
		return (double) sum / nodeSize;
	}

	/**
	 * To find the hubs which contain the highest degree
	 * 
	 * @return
	 */
	public Set<Node> findHubs() {
		List<Integer> list = degreeReverseSorter();
		int maxDegree = list.get(0);
		Set<Node> hubs = new HashSet<Node>();
		fullDistribution.forEach((key, val) -> {
			if (val == maxDegree)
				hubs.add(key);
		});
		return hubs;
	}

	/**
	 * To save degree distribution to an output file
	 * 
	 * @param filename
	 */
	public void saveDegreeDistribution(String filename) {
		List<Integer> allDegrees = degreeReverseSorter();
		Map<Integer, Integer> degreeOfNodes = getDegreeDistribution();
		Path file = Paths.get(filename);
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			writer.write("degree\tnumber of nodes\n");
			for (int degree : allDegrees) {
				writer.write(degree + "\t" + degreeOfNodes.get(degree) + "\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * To get degree distribution of the network
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getDegreeDistribution() {
		Map<Integer, Integer> degreeOfNodes = new HashMap<Integer, Integer>();
		List<Integer> allDegrees = degreeReverseSorter();
		allDegrees.forEach(degree -> degreeOfNodes.put(degree, 0));
		fullDistribution.forEach((key, val) -> degreeOfNodes.put(val, degreeOfNodes.get(val) + 1));
		return degreeOfNodes;
	}

	/**
	 * To get all possible degrees of the network and return a list in a descending
	 * order
	 * 
	 * @return
	 */
	public List<Integer> degreeReverseSorter() {
		List<Integer> allDegrees = fullDistribution.values().stream().collect(Collectors.toSet()).stream()
				.collect(Collectors.toList());
		Collections.sort(allDegrees, Collections.reverseOrder());
		return allDegrees;
	}

	/**
	 * To get the number of nodes
	 * 
	 * @return
	 */
	public int countOfNodes() {
		return nodes.size();
	}

	/**
	 * To get the number of edges
	 * 
	 * @return
	 */
	public int countOfEdges() {
		return edges.size();
	}

	/**
	 * To get the pair of the maximum degree and the hubs as string format
	 * 
	 * @return
	 */
	public Pair<Integer, String> generateHubsString() {
		int maxDegree = degreeReverseSorter().get(0);
		StringJoiner sj = new StringJoiner(",");
		findHubs().forEach(node -> sj.add(node.getName()));
		return new Pair<>(maxDegree, sj.toString());
	}
}
