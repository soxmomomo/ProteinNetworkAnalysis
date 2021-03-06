package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Hanyi.Mo
 * 
 * Network.java
 */

public class Network {
    private Set<Node> nodes;
    private Set<Edge> edges;
    private Map<Node, Integer> fullDistribution;

    public Network() {
        this.nodes = new HashSet<>();
        this.edges = new HashSet<>();
        this.fullDistribution = new HashMap<>();
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
                if (twoNodes.length != 2) {
                    throw new InvalidPropertiesFormatException(String.format("The file: %s is not in the right tab-delimited format!\nPlease input the correctly formatted file!\n", filename));
                }
                for (String str : twoNodes) {
                    nodes.add(new Node(str));
                }
                Edge tmp = new Edge(new Node(twoNodes[0]), new Node(twoNodes[1]));
                addDegreesToMap(tmp, new Node[]{new Node(twoNodes[0]), new Node(twoNodes[1])});
                edges.add(tmp);
            }
            reader.close();
        } catch (InvalidPropertiesFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException(String.format("Cannot find file: %s!\nPlease input valid filename!\n", filename));
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
    public String addInteraction(String name1, String name2) throws NullPointerException, IOException {
        if (name1 == null || name2 == null || name1.equals("") || name2.equals(""))
            throw new NullPointerException();
        Node node1 = new Node(name1);
        Node node2 = new Node(name2);
        Edge edge = new Edge(node1, node2);
        if (edges.contains(edge)) {
            throw new IOException(String.format("Interaction between %s and %s already exists!\n", node1.getName(), node2.getName()));
        }
        addDegreesToMap(edge, new Node[]{node1, node2});
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
     * @param name
     * @return
     */
    public int degreeOfNode(String name) {
        Node n = new Node(name);
        if (!nodes.contains(n))
            return 0;
        return fullDistribution.get(n);
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
            sum += degreeOfNode(node.getName());
        }
        return (double) sum / nodeSize;
    }
    
    /**
     * To get max degree
     * 
     * @return
     */
    public int getMaxDegree() {
    	return degreeReverseSorter().get(0);
    }

    /**
     * To get min degree for the position of the node in canvas
     *
     * @return
     */
    public int getMinDegree() {
        List<Integer> reversedDegrees = degreeReverseSorter();
        return reversedDegrees.get(reversedDegrees.size() - 1);
    }

    /**
     * To find the hubs which contain the highest degree
     * return a formatted string to be better handled by UI
     *
     * @return
     */
    public String findHubs() {
        int maxDegree = getMaxDegree();
        StringJoiner sj = new StringJoiner(",");
        fullDistribution.forEach((key, val) -> {
            if (val == maxDegree)
                sj.add(key.getName());
        });
        return sj.toString();
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
     * To get all possible degrees of the network and return a list in a descending order
     *
     * @return
     */
    public List<Integer> degreeReverseSorter() {
        List<Integer> allDegrees = new ArrayList<>(new HashSet<>(fullDistribution.values()));
        allDegrees.sort(Collections.reverseOrder());
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
     * Expose all edges (actually, the names of nodes connected by edges)
     * to the front-end to draw the visualised network
     *
     * @return
     */
    public List<String[]> getAllEdges() {
        List<String[]> result = new ArrayList<>();
        edges.forEach(edge -> {
            String[] arr = new String[2];
            arr[0] = edge.getNodes()[0].getName();
            arr[1] = edge.getNodes()[1].getName();
            result.add(arr);
        });
        return result;
    }
}
