import java.util.*;

public class Q4B {
  public int minRoads(int[] packages, int[][] roads) {
    int n = packages.length;

    // If there are no packages to deliver, no roads need to be traversed.
    boolean hasPackage = false;
    for (int p : packages) {
      if (p == 1) {
        hasPackage = true;
        break;
      }
    }
    if (!hasPackage)
      return 0;

    // Build an undirected graph (tree) using an adjacency list representation.
    List<List<Integer>> adj = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      adj.add(new ArrayList<>());
    }
    for (int[] road : roads) {
      int u = road[0], v = road[1];
      adj.get(u).add(v);
      adj.get(v).add(u);
    }

    // Select a “central” node to serve as the root for greedy selection
    int root = findCenter(n, adj);

    // Build a BFS tree starting from the chosen root node.
    int[] parent = new int[n];
    Arrays.fill(parent, -1);
    List<List<Integer>> children = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      children.add(new ArrayList<>());
    }
    boolean[] visited = new boolean[n];
    Queue<Integer> queue = new LinkedList<>();
    queue.offer(root);
    visited[root] = true;
    while (!queue.isEmpty()) {
      int cur = queue.poll();
      for (int nei : adj.get(cur)) {
        if (!visited[nei]) {
          visited[nei] = true;
          parent[nei] = cur;
          children.get(cur).add(nei);
          queue.offer(nei);
        }
      }
    }

    // Greedy approach: Choose "stop" nodes in postorder so that every package is
    // covered.
    // A stop can cover its own node, its immediate neighbors, and their neighbors
    // (distance 2).
    boolean[] covered = new boolean[n];
    boolean[] stop = new boolean[n];
    List<Integer> postOrder = new ArrayList<>();
    getPostOrder(root, children, postOrder);

    // Process each node in postorder: if a package location is uncovered, mark a
    // stop.
    for (int node : postOrder) {
      if (packages[node] == 1 && !covered[node]) {
        // Try to place the stop as high up the tree as possible: parent's parent if
        // available,
        // otherwise just the parent, or the node itself if necessary.
        int p = parent[node];
        int gp = (p == -1 ? -1 : parent[p]);
        int stopNode = (gp != -1 ? gp : (p != -1 ? p : node));
        stop[stopNode] = true;
        // Mark all nodes within distance 2 (from the stop node) as covered.
        markCovered(stopNode, adj, covered);
      }
    }

    // Calculate the minimal subtree containing all the stop nodes in the BFS tree.
    // For each edge in the BFS tree that leads to a subtree containing a stop,
    // count that edge.
    int[] necessaryEdges = new int[1];
    computeSubtree(root, children, stop, necessaryEdges);
    // Since each necessary edge must be traversed twice (one to go and one to
    // return),
    // we multiply the count by 2 before returning the result.
    return necessaryEdges[0] * 2;
  }

  // Find the center of the tree using a peeling process
  private int findCenter(int n, List<List<Integer>> adj) {
    int[] degree = new int[n];
    for (int i = 0; i < n; i++) {
      degree[i] = adj.get(i).size();
    }
    Queue<Integer> leaves = new LinkedList<>();
    for (int i = 0; i < n; i++) {
      if (degree[i] <= 1) {
        leaves.offer(i);
      }
    }
    int remaining = n;
    while (remaining > 2) {
      int size = leaves.size();
      remaining -= size;
      for (int i = 0; i < size; i++) {
        int leaf = leaves.poll();
        for (int nei : adj.get(leaf)) {
          degree[nei]--;
          if (degree[nei] == 1) {
            leaves.offer(nei);
          }
        }
      }
    }
    return leaves.poll(); // Return one of the centers of the tree.
  }

  // Perform DFS to obtain the postorder traversal of the BFS tree.
  private void getPostOrder(int node, List<List<Integer>> children, List<Integer> order) {
    for (int child : children.get(node)) {
      getPostOrder(child, children, order);
    }
    order.add(node); // Add the node to the postorder list after visiting its children.
  }

  // Starting from the given node, mark all nodes within a distance of 2 as
  // "covered".
  private void markCovered(int start, List<List<Integer>> adj, boolean[] covered) {
    Queue<int[]> q = new LinkedList<>();
    q.offer(new int[] { start, 0 }); // Start node with distance 0.
    boolean[] visited = new boolean[covered.length];
    visited[start] = true;
    while (!q.isEmpty()) {
      int[] curr = q.poll();
      int node = curr[0], d = curr[1];
      if (d > 2)
        continue; // Only cover nodes within distance 2.
      covered[node] = true;
      for (int nei : adj.get(node)) {
        if (!visited[nei]) {
          visited[nei] = true;
          q.offer(new int[] { nei, d + 1 });
        }
      }
    }
  }

  // Perform DFS on BFS tree & prune branches that do not contain any stop node.
  // Count each edge that leads to a subtree containing a stop node.
  private boolean computeSubtree(int node, List<List<Integer>> children, boolean[] stop, int[] count) {
    boolean hasStop = stop[node];
    for (int child : children.get(node)) {
      if (computeSubtree(child, children, stop, count)) {
        hasStop = true;
        count[0]++; // Count the edge from this node to the child.
      }
    }
    return hasStop; // Return whether this node (or its subtree) contains a stop.
  }

  // For testing the solution with sample data.
  public static void main(String[] args) {
    Q4B solver = new Q4B();
    int[] packages = { 1, 0, 0, 0, 0, 1 };
    int[][] roads = {
        { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }
    };
    int result = solver.minRoads(packages, roads);
    System.out.println("Minimum roads traversed: " + result); // Expected output is 2.

    int[] packages2 = { 1, 0, 0, 0, 0, 0, 0, 1 };
    int[][] roads2 = {
        { 0, 1 },
        { 1, 2 },
        { 2, 3 },
        { 3, 4 },
        { 4, 5 },
        { 5, 6 },
        { 6, 7 }
    };
    int result2 = solver.minRoads(packages2, roads2);
    System.out.println("Minimum roads traversed: " + result2); // Expected output is 6.
  }
}
