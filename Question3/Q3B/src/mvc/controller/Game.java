package mvc.controller;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import javax.sound.sampled.Clip;
import mvc.model.*;
import mvc.view.GamePanel;
import sounds.Sound;

public class Game implements Runnable, KeyListener {

  public static final Dimension DIM = new Dimension(500, 700);
  public static final int THRESHOLD = 2400;
  public static int nAutoDelay = 300;
  public static final int TETROMINO_NUMBER = 100;
  public static Random R = new Random();
  public final static int ANIM_DELAY = 45; // ms between repaints

  private GamePanel gmpPanel;
  private Thread thrAnim;
  private Thread thrAutoDown;
  private Thread thrLoaded;
  private long lTime;
  private long lTimeStep;
  private static final int PRESS_DELAY = 40;
  private boolean bMuted = true;

  // Key codes for keyboard control
  private final int PAUSE = 80, // P key
      QUIT = 81, // Q key
      LEFT = 37, // Left arrow
      RIGHT = 39, // Right arrow
      START = 83, // S key
      MUTE = 77, // M key
      DOWN = 40, // Down arrow
      SPACE = 32; // Space bar (rotate)

  private Clip clpMusicBackground;
  private Clip clpBomb;

  // Queue for upcoming tetrominoes
  private Queue<Tetromino> tetrominoQueue = new LinkedList<>();

  public Game() {
    // Create GamePanel and pass this controller so that the panel can call
    // startGame()
    gmpPanel = new GamePanel(DIM, this);
    // Ensure our key listener is attached
    gmpPanel.addKeyListener(this);

    // Load sounds
    clpBomb = Sound.clipForLoopFactory("explosion-02.wav");
    clpMusicBackground = Sound.clipForLoopFactory("tetris_tone_loop_1_.wav");
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
      try {
        Game game = new Game();
        game.fireUpThreads(); // Start background threads
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void fireUpThreads() {
    if (thrAnim == null) {
      thrAnim = new Thread(this);
      thrAnim.start();
    }
    if (thrAutoDown == null) {
      thrAutoDown = new Thread(this);
      thrAutoDown.start();
    }
    if (!CommandCenter.getInstance().isLoaded() && thrLoaded == null) {
      thrLoaded = new Thread(this);
      thrLoaded.start();
    }
  }

  @Override
  public void run() {
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    long lStartTime = System.currentTimeMillis();

    // Mark as loaded if using the loaded thread
    if (!CommandCenter.getInstance().isLoaded() && Thread.currentThread() == thrLoaded) {
      CommandCenter.getInstance().setLoaded(true);
    }

    // Auto-down loop
    while (Thread.currentThread() == thrAutoDown) {
      if (!CommandCenter.getInstance().isPaused() && CommandCenter.getInstance().isPlaying()) {
        tryMovingDown();
      }
      gmpPanel.repaint();
      try {
        lStartTime += nAutoDelay;
        Thread.sleep(Math.max(0, lStartTime - System.currentTimeMillis()));
      } catch (InterruptedException e) {
        break;
      }
    }

    // Animation/repaint loop
    while (Thread.currentThread() == thrAnim) {
      if (!CommandCenter.getInstance().isPaused() && CommandCenter.getInstance().isPlaying()) {
        updateGrid();
      }
      gmpPanel.repaint();
      try {
        lStartTime += ANIM_DELAY;
        Thread.sleep(Math.max(0, lStartTime - System.currentTimeMillis()));
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  private void updateGrid() {
    if (gmpPanel.tetrCurrent != null) {
      gmpPanel.grid.setBlocks(gmpPanel.tetrCurrent);
    }
  }

  private void tryMovingDown() {
    if (gmpPanel.tetrCurrent == null)
      return;

    Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
    tetrTest.moveDown();
    if (gmpPanel.grid.requestDown(tetrTest)) {
      gmpPanel.tetrCurrent.moveDown();
    }
    // Bomb piece: clear board, add score, and play bomb sound
    else if (CommandCenter.getInstance().isPlaying() && gmpPanel.tetrCurrent instanceof Bomb) {
      clpBomb.stop();
      clpBomb.flush();
      clpBomb.setFramePosition(0);
      clpBomb.start();
      gmpPanel.grid.clearGrid();
      CommandCenter.getInstance().addScore(1000);
      if (CommandCenter.getInstance().getHighScore() < CommandCenter.getInstance().getScore()) {
        CommandCenter.getInstance().setHighScore(CommandCenter.getInstance().getScore());
      }
      gmpPanel.tetrCurrent = tetrominoQueue.poll();
      if (gmpPanel.tetrCurrent == null) {
        gmpPanel.tetrCurrent = createNewTetromino();
      }
      tetrominoQueue.offer(createNewTetromino());
      gmpPanel.tetrOnDeck = tetrominoQueue.peek();
    }
    // Normal landing
    else if (CommandCenter.getInstance().isPlaying()) {
      gmpPanel.grid.addToOccupied(gmpPanel.tetrCurrent);
      gmpPanel.grid.checkTopRow();
      gmpPanel.grid.checkCompletedRow();
      gmpPanel.tetrCurrent = tetrominoQueue.poll();
      if (gmpPanel.tetrCurrent == null) {
        gmpPanel.tetrCurrent = createNewTetromino();
      }
      tetrominoQueue.offer(createNewTetromino());
      gmpPanel.tetrOnDeck = tetrominoQueue.peek();
    }
  }

  // Public helper method for moving down (for button action)
  public void moveDown() {
    long currentTime = System.currentTimeMillis();
    if (currentTime > lTimeStep + PRESS_DELAY - 35 && CommandCenter.getInstance().isPlaying()) {
      tryMovingDown();
      lTimeStep = System.currentTimeMillis();
    }
  }

  // GAME START-UP (RESET EVERYTHING)
  public void startGame() {
    gmpPanel.grid.clearGrid();
    CommandCenter.getInstance().clearAll();
    CommandCenter.getInstance().initGame();
    initializeQueue();
    CommandCenter.getInstance().setGameOver(false);
    CommandCenter.getInstance().setPlaying(true);
    CommandCenter.getInstance().setPaused(false);
    if (!bMuted) {
      clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
    }
    gmpPanel.requestFocusInWindow();
  }

  private void initializeQueue() {
    tetrominoQueue.clear();
    for (int i = 0; i < 5; i++) {
      tetrominoQueue.offer(createNewTetromino());
    }
    gmpPanel.tetrCurrent = tetrominoQueue.poll();
    if (gmpPanel.tetrCurrent == null) {
      gmpPanel.tetrCurrent = createNewTetromino();
    }
    gmpPanel.tetrOnDeck = tetrominoQueue.peek();
  }

  private Tetromino createNewTetromino() {
    int nKey = R.nextInt(TETROMINO_NUMBER);
    if (nKey >= 0 && nKey <= 12) {
      return new LongPiece();
    } else if (nKey > 12 && nKey <= 23) {
      return new SquarePiece();
    } else if (nKey > 23 && nKey <= 35) {
      return new SPiece();
    } else if (nKey > 35 && nKey <= 46) {
      return new TPiece();
    } else if (nKey > 46 && nKey <= 58) {
      return new ZPiece();
    } else if (nKey > 58 && nKey <= 71) {
      return new LPiece();
    } else if (nKey > 71 && nKey <= 84) {
      return new JPiece();
    } else if (nKey > 84 && nKey <= 98) {
      return new PlusPiece();
    } else {
      return new Bomb();
    }
  }

  private static void stopLoopingSounds(Clip... clpClips) {
    for (Clip clp : clpClips) {
      clp.stop();
    }
  }

  // KEYLISTENER METHODS
  @Override
  public void keyPressed(KeyEvent e) {
    lTime = System.currentTimeMillis();
    int nKeyPressed = e.getKeyCode();

    if (nKeyPressed == START && CommandCenter.getInstance().isLoaded() &&
        (!CommandCenter.getInstance().isPlaying() || CommandCenter.getInstance().isGameOver())) {
      startGame();
    }
    if (nKeyPressed == PAUSE && lTime > lTimeStep + PRESS_DELAY) {
      CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
      lTimeStep = System.currentTimeMillis();
    }
    if (nKeyPressed == QUIT && lTime > lTimeStep + PRESS_DELAY) {
      System.exit(0);
    }
    if (nKeyPressed == DOWN && (lTime > lTimeStep + PRESS_DELAY - 35) &&
        CommandCenter.getInstance().isPlaying()) {
      tryMovingDown();
      lTimeStep = System.currentTimeMillis();
    }
    if (nKeyPressed == RIGHT && lTime > lTimeStep + PRESS_DELAY) {
      Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
      tetrTest.moveRight();
      if (gmpPanel.grid.requestLateral(tetrTest)) {
        gmpPanel.tetrCurrent.moveRight();
        lTimeStep = System.currentTimeMillis();
      }
    }
    if (nKeyPressed == LEFT && lTime > lTimeStep + PRESS_DELAY) {
      Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
      tetrTest.moveLeft();
      if (gmpPanel.grid.requestLateral(tetrTest)) {
        gmpPanel.tetrCurrent.moveLeft();
        lTimeStep = System.currentTimeMillis();
      }
    }
    if (nKeyPressed == SPACE) { // Rotate piece
      Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
      tetrTest.rotate();
      if (gmpPanel.grid.requestLateral(tetrTest)) {
        gmpPanel.tetrCurrent.rotate();
        lTimeStep = System.currentTimeMillis();
      }
    }
    if (nKeyPressed == MUTE) {
      if (!bMuted) {
        stopLoopingSounds(clpMusicBackground, clpBomb);
        bMuted = !bMuted;
      } else {
        clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
        bMuted = !bMuted;
      }
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  // -------------------------------
  // Helper methods for button actions:
  // -------------------------------
  public void moveLeft() {
    long currentTime = System.currentTimeMillis();
    if (currentTime > lTimeStep + PRESS_DELAY) {
      Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
      tetrTest.moveLeft();
      if (gmpPanel.grid.requestLateral(tetrTest)) {
        gmpPanel.tetrCurrent.moveLeft();
        lTimeStep = System.currentTimeMillis();
      }
    }
  }

  public void moveRight() {
    long currentTime = System.currentTimeMillis();
    if (currentTime > lTimeStep + PRESS_DELAY) {
      Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
      tetrTest.moveRight();
      if (gmpPanel.grid.requestLateral(tetrTest)) {
        gmpPanel.tetrCurrent.moveRight();
        lTimeStep = System.currentTimeMillis();
      }
    }
  }

  public void rotatePiece() {
    long currentTime = System.currentTimeMillis();
    if (currentTime > lTimeStep + PRESS_DELAY) {
      Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
      tetrTest.rotate();
      if (gmpPanel.grid.requestLateral(tetrTest)) {
        gmpPanel.tetrCurrent.rotate();
        lTimeStep = System.currentTimeMillis();
      }
    }
  }

  public void togglePause() {
    long currentTime = System.currentTimeMillis();
    if (currentTime > lTimeStep + PRESS_DELAY) {
      CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
      lTimeStep = System.currentTimeMillis();
    }
  }
}
