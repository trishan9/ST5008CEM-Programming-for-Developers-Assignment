
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class NetworkOptimization extends JFrame {
  // Data structures for the graph
  private ArrayList<Vertex> vertices;
  private ArrayList<Connection> connections;
  private ArrayList<Connection> mstConnections;
  private ArrayList<Connection> spConnections;

  // UI Components
  private DrawPanel drawPanel;
  private JLabel statusLabel;
  private JComboBox<String> vertexTypeCombo;

  // Interaction Modes
  private enum Mode {
    ADD_VERTEX, ADD_CONNECTION, FIND_PATH
  }

  private Mode currentMode = Mode.ADD_VERTEX;

  // Temporary selections
  private Vertex tempVertex = null;
  private Vertex pathSource = null;
  private Vertex pathDest = null;

  // UI Colors
  private final Color PANEL_BACKGROUND = new Color(240, 240, 245);
  private final Color PRIMARY_COLOR = new Color(70, 130, 180);
  private final Color SECONDARY_COLOR = new Color(51, 51, 51);
  private final Color ACCENT_COLOR = new Color(255, 255, 255);

  public NetworkOptimization() {
    super("Graph Network Planner");
    vertices = new ArrayList<>();
    connections = new ArrayList<>();
    mstConnections = new ArrayList<>();
    spConnections = new ArrayList<>();
    setupUI();
  }

  private void setupUI() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(960, 720);
    setLayout(new BorderLayout(5, 5));

    // Top Status Panel
    JPanel topPanel = new JPanel();
    topPanel.setBackground(SECONDARY_COLOR);
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    statusLabel = new JLabel("Status: 0 vertices, 0 connections. No path computed.");
    statusLabel.setForeground(Color.WHITE);
    statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    topPanel.add(statusLabel);
    add(topPanel, BorderLayout.NORTH);

    // Drawing Panel in the center
    drawPanel = new DrawPanel();
    drawPanel.setBackground(new Color(250, 250, 250));
    add(drawPanel, BorderLayout.CENTER);

    // Bottom Navigation Panel - Refactored UI
    add(createBottomPanel(), BorderLayout.SOUTH);

    updateStatus("");
  }

  private JPanel createBottomPanel() {
    // Main bottom panel with gradient background
    JPanel bottomPanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth(), h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, 0, h, new Color(40, 80, 120));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
      }
    };
    bottomPanel.setLayout(new BorderLayout());
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    // Create card layout for the main content area
    JPanel contentPanel = new JPanel();
    contentPanel.setOpaque(false);
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

    // Add the mode selector panel
    contentPanel.add(createModePanel());

    // Add the vertex type panel
    JPanel typePanel = createVertexTypePanel();
    typePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    contentPanel.add(Box.createVerticalStrut(5));
    contentPanel.add(typePanel);

    // Add the action buttons panel
    JPanel actionPanel = createActionPanel();
    actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    contentPanel.add(Box.createVerticalStrut(5));
    contentPanel.add(actionPanel);

    bottomPanel.add(contentPanel, BorderLayout.CENTER);

    return bottomPanel;
  }

  private JPanel createModePanel() {
    JPanel modePanel = new JPanel();
    modePanel.setOpaque(false);
    modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.X_AXIS));
    modePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JToggleButton vertexBtn = createModeButton("Add Vertex", true);
    JToggleButton connectionBtn = createModeButton("Add Connection", false);
    JToggleButton pathBtn = createModeButton("Find Shortest Path", false);

    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(vertexBtn);
    modeGroup.add(connectionBtn);
    modeGroup.add(pathBtn);

    // Add listeners
    vertexBtn.addActionListener(e -> {
      currentMode = Mode.ADD_VERTEX;
      resetTempSelections();
      updateStatus("Mode changed: Add Vertex");
      drawPanel.repaint();
    });

    connectionBtn.addActionListener(e -> {
      currentMode = Mode.ADD_CONNECTION;
      resetTempSelections();
      updateStatus("Mode changed: Add Connection");
      drawPanel.repaint();
    });

    pathBtn.addActionListener(e -> {
      currentMode = Mode.FIND_PATH;
      resetTempSelections();
      updateStatus("Mode changed: Find Shortest Path");
      drawPanel.repaint();
    });

    // Add buttons with spacing
    modePanel.add(Box.createHorizontalGlue());
    modePanel.add(vertexBtn);
    modePanel.add(Box.createHorizontalStrut(10));
    modePanel.add(connectionBtn);
    modePanel.add(Box.createHorizontalStrut(10));
    modePanel.add(pathBtn);
    modePanel.add(Box.createHorizontalGlue());

    return modePanel;
  }

  private JToggleButton createModeButton(String text, boolean selected) {
    JToggleButton btn = new JToggleButton(text, selected);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 14));

    // Green 400 shade for background
    Color greenColor = new Color(76, 175, 80); // Green 400 shade

    btn.setBackground(greenColor);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);

    // Custom border for selected state
    btn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(8, 15, 8, 15),
        BorderFactory.createMatteBorder(0, 0, selected ? 2 : 0, 0, Color.WHITE)));

    // Change border on selection and add hover effect
    btn.addChangeListener(e -> {
      if (btn.isSelected()) {
        btn.setBackground(greenColor);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(8, 15, 8, 15),
            BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE)));
      } else {
        if (btn.getModel().isRollover()) {
          btn.setBackground(greenColor.brighter());
        } else {
          btn.setBackground(greenColor);
        }
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(8, 15, 8, 15),
            BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE)));
      }
    });

    // Add hover effect
    btn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        if (!btn.isSelected()) {
          btn.setBackground(greenColor.brighter());
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        if (!btn.isSelected()) {
          btn.setBackground(greenColor);
        }
      }
    });

    return btn;
  }

  private JPanel createVertexTypePanel() {
    JPanel typePanel = new JPanel();
    typePanel.setOpaque(false);
    typePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    JLabel typeLabel = new JLabel("Vertex Type:");
    typeLabel.setForeground(Color.WHITE);
    typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

    vertexTypeCombo = new JComboBox<>(new String[] { "Client", "Server" });
    vertexTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    vertexTypeCombo.setPreferredSize(new Dimension(120, 30));
    vertexTypeCombo.setBackground(ACCENT_COLOR);

    typePanel.add(typeLabel);
    typePanel.add(Box.createHorizontalStrut(10));
    typePanel.add(vertexTypeCombo);

    return typePanel;
  }

  private JPanel createActionPanel() {
    JPanel actionPanel = new JPanel();
    actionPanel.setOpaque(false);
    actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    JButton mstBtn = createActionButton("Calculate MST", new Color(46, 139, 87));
    JButton clearHighBtn = createActionButton("Clear Highlights", new Color(178, 34, 34));
    JButton clearAllBtn = createActionButton("Reset All", new Color(138, 43, 226));

    // Add action listeners
    mstBtn.addActionListener(e -> calculateMST());
    clearHighBtn.addActionListener(e -> {
      mstConnections.clear();
      spConnections.clear();
      resetTempSelections();
      updateStatus("Highlights Cleared");
      drawPanel.repaint();
    });
    clearAllBtn.addActionListener(e -> resetAll());

    actionPanel.add(mstBtn);
    actionPanel.add(Box.createHorizontalStrut(15));
    actionPanel.add(clearHighBtn);
    actionPanel.add(Box.createHorizontalStrut(15));
    actionPanel.add(clearAllBtn);

    return actionPanel;
  }

  private JButton createActionButton(String text, Color color) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btn.setForeground(Color.WHITE);
    btn.setBackground(color);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
    btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

    // Add hover effect
    btn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(color.brighter());
      }

      @Override
      public void mouseExited(MouseEvent e) {
        btn.setBackground(color);
      }
    });

    return btn;
  }

  // Update the status label.
  private void updateStatus(String extra) {
    double totalCost = 0, totalLatency = 0;
    for (Connection conn : connections) {
      totalCost += conn.cost;
      totalLatency += (1.0 / conn.bandwidth);
    }
    statusLabel.setText(String.format("%s | %d vertices, %d connections | Cost: %.2f, Latency: %.2f",
        extra, vertices.size(), connections.size(), totalCost, totalLatency));
  }

  // Reset temporary selections for connection or path modes.
  private void resetTempSelections() {
    tempVertex = null;
    pathSource = null;
    pathDest = null;
  }

  // Calculate MST using Kruskal's algorithm.
  private void calculateMST() {
    if (vertices.isEmpty() || connections.isEmpty()) {
      updateStatus("MST Error: Insufficient data.");
      return;
    }
    mstConnections.clear();
    int n = vertices.size();
    int[] parent = new int[n];
    for (int i = 0; i < n; i++)
      parent[i] = i;

    ArrayList<Connection> sorted = new ArrayList<>(connections);
    sorted.sort(Comparator.comparingDouble(c -> c.cost));

    for (Connection c : sorted) {
      int u = c.from.id;
      int v = c.to.id;
      int rootU = find(parent, u);
      int rootV = find(parent, v);
      if (rootU != rootV) {
        mstConnections.add(c);
        parent[rootU] = rootV;
      }
    }

    double mstCost = 0;
    for (Connection c : mstConnections) {
      mstCost += c.cost;
    }
    updateStatus(String.format("MST computed. MST Cost: %.2f", mstCost));
    spConnections.clear();
    drawPanel.repaint();
  }

  // Union-Find helper.
  private int find(int[] parent, int i) {
    if (parent[i] != i) {
      parent[i] = find(parent, parent[i]);
    }
    return parent[i];
  }

  // Compute the shortest path using Dijkstra's algorithm (weight = 1/bandwidth).
  private void computeShortestPath(Vertex source, Vertex dest) {
    if (source == null || dest == null)
      return;
    int n = vertices.size();
    double[] dist = new double[n];
    int[] prev = new int[n];
    Arrays.fill(dist, Double.MAX_VALUE);
    Arrays.fill(prev, -1);
    dist[source.id] = 0;

    PriorityQueue<VertexDist> pq = new PriorityQueue<>(Comparator.comparingDouble(vd -> vd.distance));
    pq.add(new VertexDist(source.id, 0));

    while (!pq.isEmpty()) {
      VertexDist vd = pq.poll();
      int u = vd.vertexId;
      if (vd.distance > dist[u])
        continue;
      for (Connection conn : connections) {
        int v = -1;
        if (conn.from.id == u)
          v = conn.to.id;
        else if (conn.to.id == u)
          v = conn.from.id;
        if (v != -1) {
          double weight = 1.0 / conn.bandwidth;
          if (dist[u] + weight < dist[v]) {
            dist[v] = dist[u] + weight;
            prev[v] = u;
            pq.add(new VertexDist(v, dist[v]));
          }
        }
      }
    }

    ArrayList<Integer> path = new ArrayList<>();
    int cur = dest.id;
    while (cur != -1) {
      path.add(cur);
      if (cur == source.id)
        break;
      cur = prev[cur];
    }
    Collections.reverse(path);
    if (path.isEmpty() || path.get(0) != source.id) {
      updateStatus("Path not found between " + source.label + " and " + dest.label);
      return;
    }

    StringBuilder pathStr = new StringBuilder("Shortest path: ");
    for (int i = 0; i < path.size(); i++) {
      pathStr.append(vertices.get(path.get(i)).label);
      if (i < path.size() - 1)
        pathStr.append(" -> ");
    }

    spConnections.clear();
    for (int i = 0; i < path.size() - 1; i++) {
      int a = path.get(i), b = path.get(i + 1);
      for (Connection conn : connections) {
        if ((conn.from.id == a && conn.to.id == b) || (conn.from.id == b && conn.to.id == a)) {
          spConnections.add(conn);
          break;
        }
      }
    }
    updateStatus(String.format("%s | Total latency: %.2f", pathStr.toString(), dist[dest.id]));
    drawPanel.repaint();
  }

  // Reset all graph data.
  private void resetAll() {
    vertices.clear();
    connections.clear();
    mstConnections.clear();
    spConnections.clear();
    resetTempSelections();
    updateStatus("All cleared.");
    drawPanel.repaint();
  }

  // Inner class for vertices (nodes)
  class Vertex {
    int id, x, y;
    String label, type; // e.g., "Client" or "Server"
    static final int DIAMETER = 30;

    public Vertex(int id, int x, int y, String type) {
      this.id = id;
      this.x = x;
      this.y = y;
      this.type = type;
      this.label = type.charAt(0) + "" + id; // e.g., C0, S1
    }

    public boolean contains(int px, int py) {
      int dx = x - px;
      int dy = y - py;
      return dx * dx + dy * dy <= (DIAMETER / 2) * (DIAMETER / 2);
    }
  }

  // Inner class for connections (edges)
  class Connection {
    Vertex from, to;
    double cost, bandwidth;

    public Connection(Vertex from, Vertex to, double cost, double bandwidth) {
      this.from = from;
      this.to = to;
      this.cost = cost;
      this.bandwidth = bandwidth;
    }
  }

  // Helper class for Dijkstra's algorithm
  class VertexDist {
    int vertexId;
    double distance;

    public VertexDist(int vertexId, double distance) {
      this.vertexId = vertexId;
      this.distance = distance;
    }
  }

  // Custom panel that handles drawing and mouse interactions
  class DrawPanel extends JPanel implements MouseListener {
    public DrawPanel() {
      addMouseListener(this);
      setBackground(new Color(255, 255, 255));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Draw connections (edges)
      for (Connection conn : connections) {
        if (mstConnections.contains(conn)) {
          g2.setColor(Color.RED);
          g2.setStroke(new BasicStroke(3));
        } else if (spConnections.contains(conn)) {
          g2.setColor(Color.BLUE);
          g2.setStroke(new BasicStroke(2));
        } else {
          g2.setColor(Color.GRAY);
          g2.setStroke(new BasicStroke(1));
        }
        g2.drawLine(conn.from.x, conn.from.y, conn.to.x, conn.to.y);
        int midX = (conn.from.x + conn.to.x) / 2;
        int midY = (conn.from.y + conn.to.y) / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(String.format("C:%.1f B:%.1f", conn.cost, conn.bandwidth), midX, midY);
      }

      // Draw vertices (nodes)
      for (Vertex v : vertices) {
        if (v.type.equals("Server")) {
          g2.setColor(new Color(102, 205, 170)); // Medium Aquamarine
        } else {
          g2.setColor(new Color(135, 206, 250)); // Light Sky Blue
        }
        g2.fillOval(v.x - Vertex.DIAMETER / 2, v.y - Vertex.DIAMETER / 2, Vertex.DIAMETER, Vertex.DIAMETER);
        g2.setColor(Color.BLACK);
        g2.drawOval(v.x - Vertex.DIAMETER / 2, v.y - Vertex.DIAMETER / 2, Vertex.DIAMETER, Vertex.DIAMETER);
        g2.drawString(v.label, v.x - 10, v.y + 5);

        // If vertex is selected in FIND_PATH mode, highlight it.
        if (currentMode == Mode.FIND_PATH && (v == pathSource || v == pathDest)) {
          g2.setColor(Color.ORANGE);
          g2.setStroke(new BasicStroke(2));
          g2.drawOval(v.x - Vertex.DIAMETER / 2 - 3, v.y - Vertex.DIAMETER / 2 - 3, Vertex.DIAMETER + 6,
              Vertex.DIAMETER + 6);
        }
      }

      // In ADD_CONNECTION mode, if a vertex is already selected, highlight it.
      if (currentMode == Mode.ADD_CONNECTION && tempVertex != null) {
        g2.setColor(Color.MAGENTA);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(tempVertex.x - Vertex.DIAMETER / 2 - 3, tempVertex.y - Vertex.DIAMETER / 2 - 3, Vertex.DIAMETER + 6,
            Vertex.DIAMETER + 6);
      }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      int x = e.getX(), y = e.getY();
      if (currentMode == Mode.ADD_VERTEX) {
        String type = (String) vertexTypeCombo.getSelectedItem();
        Vertex newV = new Vertex(vertices.size(), x, y, type);
        vertices.add(newV);
        updateNetworkInfo();
        repaint();
      } else if (currentMode == Mode.ADD_CONNECTION) {
        Vertex clicked = getVertexAt(x, y);
        if (clicked != null) {
          if (tempVertex == null) {
            tempVertex = clicked;
            repaint();
          } else if (tempVertex != clicked) {
            String input = JOptionPane.showInputDialog(NetworkOptimization.this,
                "Enter cost and bandwidth (comma separated):",
                "Connection Details", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
              try {
                String[] parts = input.split(",");
                double cost = Double.parseDouble(parts[0].trim());
                double bandwidth = Double.parseDouble(parts[1].trim());
                Connection conn = new Connection(tempVertex, clicked, cost, bandwidth);
                connections.add(conn);
                updateNetworkInfo();
              } catch (Exception ex) {
                JOptionPane.showMessageDialog(NetworkOptimization.this,
                    "Invalid input. Please enter numeric cost and bandwidth.");
              }
            }
            tempVertex = null;
            repaint();
          }
        }
      } else if (currentMode == Mode.FIND_PATH) {
        Vertex clicked = getVertexAt(x, y);
        if (clicked != null) {
          if (pathSource == null) {
            pathSource = clicked;
            updateStatus("Source selected: " + clicked.label);
            repaint();
          } else if (pathDest == null && clicked != pathSource) {
            pathDest = clicked;
            updateStatus("Destination selected: " + clicked.label);
            computeShortestPath(pathSource, pathDest);
            resetTempSelections();
          } else {
            updateStatus("Source and destination cannot be the same.");
          }
        }
      }
    }

    private Vertex getVertexAt(int x, int y) {
      for (Vertex v : vertices) {
        if (v.contains(x, y))
          return v;
      }
      return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
  }

  // Update the top status with network summary
  private void updateNetworkInfo() {
    double totalCost = 0, totalLatency = 0;
    for (Connection conn : connections) {
      totalCost += conn.cost;
      totalLatency += (1.0 / conn.bandwidth);
    }
    statusLabel.setText(String.format("Network Info: %d vertices, %d connections | Cost: %.2f, Latency: %.2f",
        vertices.size(), connections.size(), totalCost, totalLatency));
  }

  // Main method
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    SwingUtilities.invokeLater(() -> new NetworkOptimization().setVisible(true));
  }
}
