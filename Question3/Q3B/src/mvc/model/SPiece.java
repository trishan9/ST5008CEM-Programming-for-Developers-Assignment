package mvc.model;

import java.awt.*;

public class SPiece extends Tetromino {
  public SPiece() {
    super();
    initialize();
    mColor = Color.magenta;
  }

  public void initialize() {
    for (int i = 0; i < DIM; i++) {
      if (i == 0) {
        mColoredSquares[i][0][0] = false;
        mColoredSquares[i][0][1] = false;
        mColoredSquares[i][0][2] = true;
        mColoredSquares[i][0][3] = false;

        mColoredSquares[i][1][0] = false;
        mColoredSquares[i][1][1] = true;
        mColoredSquares[i][1][2] = true;
        mColoredSquares[i][1][3] = false;

        mColoredSquares[i][2][0] = false;
        mColoredSquares[i][2][1] = true;
        mColoredSquares[i][2][2] = false;
        mColoredSquares[i][2][3] = false;

        mColoredSquares[i][3][0] = false;
        mColoredSquares[i][3][1] = false;
        mColoredSquares[i][3][2] = false;
        mColoredSquares[i][3][3] = false;

      } else if (i == 1) {
        mColoredSquares[i][0][0] = false;
        mColoredSquares[i][0][1] = false;
        mColoredSquares[i][0][2] = false;
        mColoredSquares[i][0][3] = false;

        mColoredSquares[i][1][0] = false;
        mColoredSquares[i][1][1] = true;
        mColoredSquares[i][1][2] = true;
        mColoredSquares[i][1][3] = false;

        mColoredSquares[i][2][0] = false;
        mColoredSquares[i][2][1] = false;
        mColoredSquares[i][2][2] = true;
        mColoredSquares[i][2][3] = true;

        mColoredSquares[i][3][0] = false;
        mColoredSquares[i][3][1] = false;
        mColoredSquares[i][3][2] = false;
        mColoredSquares[i][3][3] = false;
      } else if (i == 2) {
        mColoredSquares[i][0][0] = false;
        mColoredSquares[i][0][1] = false;
        mColoredSquares[i][0][2] = true;
        mColoredSquares[i][0][3] = false;

        mColoredSquares[i][1][0] = false;
        mColoredSquares[i][1][1] = true;
        mColoredSquares[i][1][2] = true;
        mColoredSquares[i][1][3] = false;

        mColoredSquares[i][2][0] = false;
        mColoredSquares[i][2][1] = true;
        mColoredSquares[i][2][2] = false;
        mColoredSquares[i][2][3] = false;

        mColoredSquares[i][3][0] = false;
        mColoredSquares[i][3][1] = false;
        mColoredSquares[i][3][2] = false;
        mColoredSquares[i][3][3] = false;

      } else {
        mColoredSquares[i][0][0] = false;
        mColoredSquares[i][0][1] = false;
        mColoredSquares[i][0][2] = false;
        mColoredSquares[i][0][3] = false;

        mColoredSquares[i][1][0] = false;
        mColoredSquares[i][1][1] = true;
        mColoredSquares[i][1][2] = true;
        mColoredSquares[i][1][3] = false;

        mColoredSquares[i][2][0] = false;
        mColoredSquares[i][2][1] = false;
        mColoredSquares[i][2][2] = true;
        mColoredSquares[i][2][3] = true;

        mColoredSquares[i][3][0] = false;
        mColoredSquares[i][3][1] = false;
        mColoredSquares[i][3][2] = false;
        mColoredSquares[i][3][3] = false;

      }
    }

  }
}
