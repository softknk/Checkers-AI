package de;

import java.util.Map;

public interface BoardFunction {

    int ERROR = -1;

    // pieces
    int NONE = 0;
    int WHITE = 1;
    int BLACK = 2;
    int WHITE_KING = 3;
    int BLACK_KING = 4;

    // player
    int WHITE_PLAYER = 5;
    int BLACK_PLAYER = 6;
    int NONE_PLAYER = 7; // stands for all NONE pieces

    void initBoard();
    int getPieceOnPosition(int row, int col);
    int getPlayerOnPosition(int row, int col);
    int getOppositePlayer(int player); // enemy
    int getPiecesLeft(int player); // how many pieces of the given player are left on the board
    void setPieceOnPosition(int piece, int row, int col);
    Map<int[], Map<int[], int[][]>> getPossibleMovesOfPiece(int row, int col);
    Map<int[], Map<int[], int[][]>> getPossibleMovesOfPlayer(int player);
    void findMoveAndApply(int player, int[] from, int[] to); //0: true 1: false 2: double-jump
    Board copyBoardAndMakeMove(int[] from, int[] to, int[][] remove_pieces);
    void applyMove(int[] from, int[] to, int[][] remove_pieces);
    boolean isValidPosition(int row, int col);

    Board copy();
    double evaluate();
    int won();
}
