import java.util.*;

/*
You have a network of n devices. Each device can have its own communication module installed at a
cost of modules [i - 1]. Alternatively, devices can communicate with each other using direct connections.
The cost of connecting two devices is given by the array connections where each connections[j] =
[device1j, device2j, costj] represents the cost to connect devices device1j and device2j. Connections are
bidirectional, and there could be multiple valid connections between the same two devices with different
costs.

Goal: Determine the minimum total cost to connect all devices in the network.

Input:
n: The number of devices.
modules: An array of costs to install communication modules on each device.
connections: An array of connections, where each connection is represented as a triplet [device1j, device2j, costj].

Output:
The minimum total cost to connect all devices.

Example:
Input: n = 3, modules = [1, 2, 2], connections = [[1, 2, 1], [2, 3, 1]] Output: 3

Explanation:
The best strategy is to install a communication module on the first device with cost 1 and connect the
other devices to it with cost 2, resulting in a total cost of 3.
*/

public class Q3A {
  // Union-Find (Disjoint Set Union) data structure with path compression and
  // union by rank.
  static class UnionFind {
    int[] parent, rank;

    public UnionFind(int n) {
      parent = new int[n];
      rank = new int[n];
      // Initially, each element is its own parent (self-loop), and the rank is 0.
      for (int i = 0; i < n; i++) {
        parent[i] = i;
      }
    }

    // Find operation with path compression: Recursively find the root of the set,
    // and apply path compression to flatten the structure, making future find
    // operations faster.
    public int find(int x) {
      if (parent[x] != x) {
        parent[x] = find(parent[x]); // Path compression
      }
      return parent[x];
    }

    // Union operation with union by rank: Connect two disjoint sets by rank.
    // The tree with a smaller rank is attached to the root of the tree with a
    // larger rank.
    public boolean union(int x, int y) {
      int rootX = find(x);
      int rootY = find(y);

      // If both elements are in the same set, no union is needed.
      if (rootX == rootY)
        return false;

      // Union by rank: Attach the smaller tree to the root of the larger tree.
      if (rank[rootX] > rank[rootY]) {
        parent[rootY] = rootX;
      } else if (rank[rootX] < rank[rootY]) {
        parent[rootX] = rootY;
      } else {
        parent[rootY] = rootX;
        rank[rootX]++; // If both trees have the same rank, increment the rank of the new root.
      }
      return true;
    }
  }

  // Class to represent an edge (connection) in the graph.
  static class Connection {
    int cost; // Cost to establish this connection.
    int device1; // One device of the connection (zero-indexed).
    int device2; // The other device of the connection (zero-indexed).

    public Connection(int cost, int device1, int device2) {
      this.cost = cost;
      this.device1 = device1;
      this.device2 = device2;
    }
  }

  // Method to calculate the minimum total cost to connect all devices using
  // Kruskal's algorithm for Minimum Spanning Tree (MST).
  public int minimumCost(int n, int[] modules, int[][] connections) {
    List<Connection> edges = new ArrayList<>();

    // Create edges from each device to a virtual "super device" (node n), where
    // the connection cost is equal to the module installation cost for that device.
    for (int i = 0; i < n; i++) {
      edges.add(new Connection(modules[i], i, n));
    }

    // Add the given connections between devices to the edges list.
    // Note: The provided device indices are 1-based, so they are converted to
    // 0-based.
    for (int[] conn : connections) {
      edges.add(new Connection(conn[2], conn[0] - 1, conn[1] - 1));
    }

    // Sort all edges by their cost in ascending order (essential for Kruskal's
    // algorithm).
    edges.sort(Comparator.comparingInt(e -> e.cost));

    // Initialize Union-Find to manage n devices and the virtual super device (n+1
    // nodes).
    UnionFind uf = new UnionFind(n + 1);
    int totalCost = 0;
    int edgesUsed = 0;

    // Apply Kruskal's algorithm: iteratively add edges to the MST while ensuring no
    // cycles.
    for (Connection edge : edges) {
      if (uf.union(edge.device1, edge.device2)) {
        totalCost += edge.cost; // Add the cost of the edge to the total.
        edgesUsed++; // Track how many edges have been added to the MST.

        // An MST with n+1 nodes requires exactly n edges, so we stop once we have n
        // edges.
        if (edgesUsed == n) {
          break;
        }
      }
    }

    return totalCost; // Return the total cost of the MST.
  }

  public static void main(String[] args) {
    Q3A solution = new Q3A();

    // Example test case:
    int n = 3; // 3 devices
    int[] modules = { 1, 2, 2 }; // Module installation costs for each device.
    int[][] connections = { { 1, 2, 1 }, { 2, 3, 1 } }; // Connections between devices.

    // The minimum cost to connect all devices with Kruskal's algorithm.
    System.out.println(solution.minimumCost(n, modules, connections)); // Expected output: 3
  }
}
