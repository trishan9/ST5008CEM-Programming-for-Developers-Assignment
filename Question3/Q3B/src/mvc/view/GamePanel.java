package mvc.view;

import java.awt.*;
import javax.swing.*;
import mvc.controller.Game;
import mvc.model.*;

public class GamePanel extends JPanel {

  private Dimension dimOff;
  private Image imgOff;
  private Graphics grpOff;

  private Game game;
  public Grid grid = new Grid();
  private GameFrame gmf;
  private Font fnt = new Font("SansSerif", Font.BOLD, 12);
  private Font fntBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
  private FontMetrics fmt;
  private int nFontWidth;
  private int nFontHeight;
  private String strDisplay = "";

  public Tetromino tetrOnDeck;
  public Tetromino tetrCurrent;

  private JLayeredPane layeredPane;
  private JPanel gameOverPanel;
  private JButton playAgainButton;
  private JPanel sidePanel;

  // Panel for grouping directional buttons (left, right, down)
  private JPanel directionalPanel;

  public GamePanel(Dimension dim, Game game) {
    this.game = game;

    setPreferredSize(dim);
    setLayout(new BorderLayout());

    layeredPane = new JLayeredPane();
    layeredPane.setPreferredSize(dim);
    add(layeredPane, BorderLayout.CENTER);

    JPanel gamePanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GamePanel.this.paintGame(g);
      }
    };
    gamePanel.setOpaque(false);
    gamePanel.setBounds(0, 0, dim.width, dim.height);
    layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

    initializeGameOverPanel(dim);
    createSidePanel(dim);

    gmf = new GameFrame();
    gmf.getContentPane().add(this);
    gmf.pack();

    initView();
    gmf.setTitle("Tetris");
    gmf.setResizable(false);
    gmf.setVisible(true);

    this.setFocusable(true);
    this.requestFocusInWindow();
  }

  private void initializeGameOverPanel(Dimension dim) {
    gameOverPanel = new JPanel(null) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
        g.drawString(gameOverText, x, getHeight() / 3);

        g.setFont(new Font("Arial", Font.BOLD, 32));
        String scoreText = "Score: " + CommandCenter.getInstance().getScore();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(scoreText)) / 2;
        g.drawString(scoreText, x, getHeight() / 3 + 60);
      }
    };
    gameOverPanel.setBounds(0, 0, dim.width, dim.height);
    gameOverPanel.setOpaque(false);
    layeredPane.add(gameOverPanel, JLayeredPane.PALETTE_LAYER);
    gameOverPanel.setVisible(false);

    playAgainButton = new JButton("Play Again");
    playAgainButton.setFont(new Font("Arial", Font.BOLD, 16));
    playAgainButton.setForeground(Color.WHITE);
    playAgainButton.setBackground(new Color(59, 130, 246));
    playAgainButton.setFocusPainted(false);
    playAgainButton.setBorderPainted(false);

    int buttonWidth = 160;
    int buttonHeight = 40;
    playAgainButton.setBounds(
        (dim.width - buttonWidth) / 2,
        dim.height / 2 + 50,
        buttonWidth,
        buttonHeight);

    playAgainButton.addActionListener(e -> {
      restartGame();
      requestFocusInWindow();
    });
    gameOverPanel.add(playAgainButton);
  }

  private void createSidePanel(Dimension dim) {
    sidePanel = new JPanel();
    sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
    sidePanel.setBackground(Color.LIGHT_GRAY);
    sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    sidePanel.setPreferredSize(new Dimension(150, dim.height));

    JLabel nextPieceLabel = new JLabel("NEXT PIECE");
    nextPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    nextPieceLabel.setFont(new Font("Arial", Font.BOLD, 16));
    sidePanel.add(nextPieceLabel);

    sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));

    // Next piece drawing area (using the same drawing as before)
    JPanel nextPiecePanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        if (tetrOnDeck != null) {
          int blockSize = Math.min(getWidth(), getHeight()) / Grid.DIM;
          boolean[][] shape = tetrOnDeck.getColoredSquares(tetrOnDeck.getOrientation());
          Color c = tetrOnDeck.getColor();
          for (int i = 0; i < Grid.DIM; i++) {
            for (int j = 0; j < Grid.DIM; j++) {
              if (shape[j][i]) {
                g.setColor(c);
                g.fill3DRect(i * blockSize, j * blockSize, blockSize, blockSize, true);
              }
            }
          }
        }
      }
    };
    nextPiecePanel.setPreferredSize(new Dimension(130, 130));
    nextPiecePanel.setMaximumSize(new Dimension(130, 130));
    nextPiecePanel.setBackground(Color.DARK_GRAY);
    sidePanel.add(nextPiecePanel);

    sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Create a directional panel to group left, down, right buttons
    directionalPanel = new JPanel(new BorderLayout(5, 5));
    directionalPanel.setMaximumSize(new Dimension(130, 80));
    directionalPanel.setOpaque(false);
    JButton btnLeft = createStyledButton("←");
    JButton btnRight = createStyledButton("→");
    JButton btnDown = createStyledButton("↓");
    directionalPanel.add(btnLeft, BorderLayout.WEST);
    directionalPanel.add(btnRight, BorderLayout.EAST);
    directionalPanel.add(btnDown, BorderLayout.SOUTH);
    sidePanel.add(directionalPanel);

    sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Other control buttons
    JButton btnRotate = createStyledButton("Rotate");
    sidePanel.add(btnRotate);
    sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
    JButton btnPause = createStyledButton("Pause");
    sidePanel.add(btnPause);
    sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
    JButton btnRestart = createStyledButton("Restart");
    sidePanel.add(btnRestart);
    sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
    JButton btnQuit = createStyledButton("Quit");
    sidePanel.add(btnQuit);

    add(sidePanel, BorderLayout.EAST);
    sidePanel.setVisible(false);
  }

  private JButton createStyledButton(String label) {
    JButton button = new JButton(label);
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setMaximumSize(new Dimension(130, 30));
    button.setFont(new Font("Arial", Font.BOLD, 14));
    button.setForeground(Color.WHITE);
    button.setBackground(new Color(59, 130, 246));
    button.setFocusPainted(false);
    button.setBorderPainted(false);

    // Use switch-case to assign appropriate action, then request focus
    switch (label) {
      case "←":
        button.addActionListener(e -> {
          game.moveLeft();
          requestFocusInWindow();
        });
        break;
      case "→":
        button.addActionListener(e -> {
          game.moveRight();
          requestFocusInWindow();
        });
        break;
      case "↓":
        button.addActionListener(e -> {
          game.moveDown();
          requestFocusInWindow();
        });
        break;
      case "Rotate":
        button.addActionListener(e -> {
          game.rotatePiece();
          requestFocusInWindow();
        });
        break;
      case "Pause":
        button.addActionListener(e -> {
          game.togglePause();
          requestFocusInWindow();
        });
        break;
      case "Restart":
        button.addActionListener(e -> {
          game.startGame();
          requestFocusInWindow();
        });
        break;
      case "Quit":
        button.addActionListener(e -> System.exit(0));
        break;
    }

    return button;
  }

  private void restartGame() {
    gameOverPanel.setVisible(false);
    game.startGame();
    requestFocusInWindow();
  }

  private void paintGame(Graphics g) {
    Dimension d = getSize();
    if (dimOff == null || grpOff == null ||
        d.width != dimOff.width || d.height != dimOff.height) {
      dimOff = d;
      imgOff = createImage(d.width, d.height);
      grpOff = imgOff.getGraphics();
    }

    grpOff.setColor(Color.blue);
    grpOff.fillRect(0, 0, d.width, d.height);

    grpOff.setColor(Color.white);
    grpOff.setFont(fnt);

    if (CommandCenter.getInstance().isGameOver()) {
      gameOverPanel.setVisible(true);
      sidePanel.setVisible(false);
    } else {
      gameOverPanel.setVisible(false);

      if (!CommandCenter.getInstance().isPlaying()) {
        sidePanel.setVisible(false);
        if (!CommandCenter.getInstance().isLoaded()) {
          String strDisplay = "Loading sounds... ";
          grpOff.drawString(strDisplay,
              (d.width - fmt.stringWidth(strDisplay)) / 2,
              d.height / 4);
        } else {
          displayStartText();
        }
      } else {
        sidePanel.setVisible(true);
        if (CommandCenter.getInstance().isPaused()) {
          String strDisplay = "Game Paused";
          grpOff.drawString(strDisplay,
              (d.width - fmt.stringWidth(strDisplay)) / 2,
              d.height / 4);
        }
        drawGameScreen(d);
      }
    }

    g.drawImage(imgOff, 0, 0, this);
  }

  private void initView() {
    Graphics g = getGraphics();
    g.setFont(fnt);
    fmt = g.getFontMetrics();
    nFontWidth = fmt.getMaxAdvance();
    nFontHeight = fmt.getHeight();
    g.setFont(fntBig);
  }

  private void displayStartText() {
    grpOff.setFont(fntBig);
    grpOff.setColor(Color.WHITE);
    String strDisplay = "TETRIS";
    int titleX = (dimOff.width - fmt.stringWidth(strDisplay)) / 2;
    grpOff.drawString(strDisplay, titleX, dimOff.height / 4);

    grpOff.setFont(fnt);
    String[] instructions = {
        "Use arrow keys to move the pieces",
        "Use space bar to rotate the piece",
        "Black squares are bombs and will clear the board",
        "'S' to Start",
        "'P' to Pause",
        "'Q' to Quit",
        "'M' to Mute or Play Music"
    };

    int yPos = dimOff.height / 4 + nFontHeight + 40;
    for (String instruction : instructions) {
      int x = (dimOff.width - fmt.stringWidth(instruction)) / 2;
      grpOff.drawString(instruction, x, yPos);
      yPos += 40;
    }
  }

  private void drawGameScreen(Dimension d) {
    int nBy = (d.height - 150) / Grid.ROWS;
    int nBx = (d.width - 150) / Grid.COLS;
    Block[][] b = grid.getBlocks();

    for (int i = 0; i < b.length; i++) {
      for (int j = 0; j < b[0].length; j++) {
        grpOff.setColor(b[i][j].getColor());
        grpOff.fill3DRect(j * nBx, i * nBy + 150, nBx, nBy, true);
      }
    }

    grpOff.setColor(Color.white);
    grpOff.draw3DRect(d.width - 150, 0, 150, d.height, true);
    grpOff.draw3DRect(d.width - 140, 10, 130, 130, true);

    // The next piece is now drawn in the side panel area
    drawScore(grpOff);
  }

  private void drawScore(Graphics g) {
    g.setColor(Color.white);
    g.setFont(fnt);
    String scoreText = "SCORE: " + CommandCenter.getInstance().getScore()
        + "    HIGH SCORE: " + CommandCenter.getInstance().getHighScore();
    g.drawString(scoreText, nFontWidth, nFontHeight);
  }

  public GameFrame getFrm() {
    return this.gmf;
  }

  public void setFrm(GameFrame frm) {
    this.gmf = frm;
  }
}
