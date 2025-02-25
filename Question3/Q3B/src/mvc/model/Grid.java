package mvc.model;

import java.awt.*;
import java.util.Stack;

public class Grid {

  public static final int ROWS = 20;
  public static final int COLS = 12;
  public static final int DIM = 4; // tetromino dimension

  // Use a stack where each element is a row (an array of Blocks)
  private Stack<Block[]> boardStack;
  // This 2D array represents the current board state for drawing
  // It is built by overlaying the falling tetromino on top of the boardStack.
  private Block[][] displayBoard;

  public Grid() {
    boardStack = new Stack<>();
    // Initialize the board with empty rows (row 0 at the top)
    for (int i = 0; i < ROWS; i++) {
      boardStack.push(createEmptyRow(i));
    }
    // Initially, displayBoard simply reflects the fixed board.
    displayBoard = boardStackToArrayCopy();
  }

  // Create a new empty row with blue blocks.
  private Block[] createEmptyRow(int rowIndex) {
    Block[] row = new Block[COLS];
    for (int j = 0; j < COLS; j++) {
      row[j] = new Block(false, Color.blue, rowIndex, j);
    }
    return row;
  }

  // Helper method: make a deep copy of boardStack as a 2D array.
  private Block[][] boardStackToArrayCopy() {
    Block[][] board = new Block[ROWS][COLS];
    for (int i = 0; i < ROWS; i++) {
      Block[] row = boardStack.get(i);
      board[i] = new Block[COLS];
      for (int j = 0; j < COLS; j++) {
        Block b = row[j];
        // Create a new Block with the same properties.
        board[i][j] = new Block(b.isOccupied(), b.getColor(), b.getRow(), b.getCol());
      }
    }
    return board;
  }

  // Returns the current board state (with falling tetromino overlaid)
  public Block[][] getBlocks() {
    return displayBoard;
  }

  // Clears the grid by resetting the stack and display board.
  public synchronized void clearGrid() {
    boardStack.clear();
    for (int i = 0; i < ROWS; i++) {
      boardStack.push(createEmptyRow(i));
    }
    displayBoard = boardStackToArrayCopy();
  }

  // Add a tetromino (once landed) to the boardStack.
  public synchronized void addToOccupied(Tetromino tetr) {
    boolean[][] shape = tetr.getColoredSquares(tetr.mOrientation);
    for (int i = 0; i < Tetromino.DIM; i++) {
      for (int j = 0; j < Tetromino.DIM; j++) {
        if (shape[i][j]) {
          int rowIndex = tetr.mRow + i;
          int colIndex = tetr.mCol + j;
          if (rowIndex >= 0 && rowIndex < ROWS && colIndex >= 0 && colIndex < COLS) {
            Block[] row = boardStack.get(rowIndex);
            row[colIndex] = new Block(true, tetr.mColor, rowIndex, colIndex);
          }
        }
      }
    }
    // Update the display board to match fixed board.
    displayBoard = boardStackToArrayCopy();
  }

  // Request to move tetromino down (collision detection).
  public synchronized boolean requestDown(Tetromino tetr) {
    boolean[][] shape = tetr.getColoredSquares(tetr.mOrientation);
    for (int i = tetr.mCol; i < tetr.mCol + DIM; i++) {
      for (int j = tetr.mRow; j < tetr.mRow + DIM; j++) {
        if (shape[j - tetr.mRow][i - tetr.mCol]) {
          if (j >= ROWS || boardStack.get(j)[i].isOccupied()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  // Request to move tetromino laterally (collision detection).
  public synchronized boolean requestLateral(Tetromino tetr) {
    boolean[][] shape = tetr.getColoredSquares(tetr.mOrientation);
    for (int i = tetr.mCol; i < tetr.mCol + DIM; i++) {
      for (int j = tetr.mRow; j < tetr.mRow + DIM; j++) {
        if (shape[j - tetr.mRow][i - tetr.mCol]) {
          if (i < 0 || i >= COLS || j >= ROWS || boardStack.get(j)[i].isOccupied()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  // Checks the top row to determine if the game is over.
  public synchronized void checkTopRow() {
    Block[] topRow = boardStack.get(0);
    for (Block b : topRow) {
      if (b.isOccupied()) {
        CommandCenter.getInstance().setPlaying(false);
        CommandCenter.getInstance().setGameOver(true);
        clearGrid();
        break;
      }
    }
  }

  // Check for and clear any completed rows.
  public synchronized void checkCompletedRow() {
    // Iterate from bottom to top.
    for (int i = ROWS - 1; i >= 0; i--) {
      Block[] row = boardStack.get(i);
      boolean complete = true;
      for (Block b : row) {
        if (!b.isOccupied()) {
          complete = false;
          break;
        }
      }
      if (complete) {
        // Add score for each block in the completed row.
        for (Block b : row) {
          CommandCenter.getInstance().addScore(b.getPoints());
        }
        if (CommandCenter.getInstance().getScore() > CommandCenter.getInstance().getHighScore()) {
          CommandCenter.getInstance().setHighScore(CommandCenter.getInstance().getScore());
        }
        // Remove the full row and add a new empty row at the top.
        boardStack.remove(i);
        boardStack.add(0, createEmptyRow(0));
      }
    }
    // Update the display board to match the new boardStack.
    displayBoard = boardStackToArrayCopy();
  }

  // Overlay the falling tetromino onto a copy of the fixed board.
  // This display board is then used for drawing.
  public synchronized void setBlocks(Tetromino tetr) {
    // Start with a fresh copy of the fixed board.
    displayBoard = boardStackToArrayCopy();
    boolean[][] shape = tetr.getColoredSquares(tetr.mOrientation);
    for (int i = tetr.mRow; i < tetr.mRow + Tetromino.DIM; i++) {
      for (int j = tetr.mCol; j < tetr.mCol + Tetromino.DIM; j++) {
        if (shape[i - tetr.mRow][j - tetr.mCol]) {
          // Overlay the falling tetromino block.
          if (i >= 0 && i < ROWS && j >= 0 && j < COLS) {
            displayBoard[i][j] = new Block(false, tetr.mColor, i, j);
          }
        }
      }
    }
  }
}
