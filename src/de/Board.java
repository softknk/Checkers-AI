package de;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Board implements BoardFunction {

    private int[][] board;

    public Board(int size) {
        board = new int[size][size];
    }

    @Override
    public void initBoard() {
        // init black pieces
        for (int i = 0; i < board.length/2 - 1; i++) {
            for (int j = 0; j < board.length; j++) if (i % 2 == 0 && j % 2 != 0 || i % 2 != 0 && j % 2 == 0) setPieceOnPosition(BLACK, i, j);
        }

        // init white pieces
        for (int i = board.length/2 + 1; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) if (i % 2 == 0 && j % 2 != 0 || i % 2 != 0 && j % 2 == 0) setPieceOnPosition(WHITE, i, j);
        }
    }

    @Override
    public int getPieceOnPosition(int row, int col) {
        if (isValidPosition(row, col)) return board[row][col];
        else return ERROR;
    }

    @Override
    public int getPlayerOnPosition(int row, int col) {
        int piece = getPieceOnPosition(row, col);
        return (piece == WHITE || piece == WHITE_KING) ? WHITE_PLAYER : (piece == BLACK || piece == BLACK_KING) ? BLACK_PLAYER : (piece == NONE) ? NONE_PLAYER : ERROR;
    }

    @Override
    public int getOppositePlayer(int player) {
        return (player == WHITE_PLAYER) ? BLACK_PLAYER : (player == BLACK_PLAYER) ? WHITE_PLAYER : ERROR;
    }

    @Override
    public int getPiecesLeft(int piece) {
        int left = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) if (getPieceOnPosition(i, j) == piece) left++;
        }
        return left;
    }

    @Override
    public void setPieceOnPosition(int piece, int row, int col) {
        if (isValidPosition(row, col)) board[row][col] = piece;
    }

    @Override
    public Map<int[], Map<int[], int[][]>> getPossibleMovesOfPiece(int row, int col) {
        int piece = getPieceOnPosition(row, col);
        int player = getPlayerOnPosition(row, col);

        if (piece == WHITE_KING || piece == BLACK_KING) {
            return getPossibleKingMoves(player, row, col);
        } else {
            Map<int[], Map<int[], int[][]>> possible_moves = new HashMap<>();

            int[] left = (player == WHITE_PLAYER) ? new int[]{row-1, col-1} : new int[]{row+1, col-1};
            int[] right = (player == WHITE_PLAYER) ? new int[]{row-1, col+1} : new int[]{row+1, col+1};
            int left_player = getPlayerOnPosition(left[0], left[1]);
            int right_player = getPlayerOnPosition(right[0], right[1]);

            if (left_player == ERROR && right_player == ERROR || left_player == player && right_player == player) return possible_moves;
            if (left_player == NONE_PLAYER) possible_moves.put(new int[]{row, col}, getMapWithoutRemovePieces(left));
            if (right_player == NONE_PLAYER) possible_moves.put(new int[]{row, col}, getMapWithoutRemovePieces(right));

            // get all moves with jumps
            getPossibleJumpMoves(player, row, col, new HashMap<>(), new LinkedList<>()).forEach((key, value) -> possible_moves.put(new int[]{row, col}, getMapOfElem(key, value)));

            return possible_moves;
        }
    }

    private Map<int[], int[][]> getMapOfElem(int[] a, int[][] b) {
        Map<int[], int[][]> c = new HashMap<>();
        c.put(a, b);
        return c;
    }

    @Override
    public Map<int[], Map<int[], int[][]>> getPossibleMovesOfPlayer(int player) {
        Map<int[], Map<int[], int[][]>> possible_moves = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) if (getPlayerOnPosition(i, j) == player) possible_moves = merge(possible_moves, getPossibleMovesOfPiece(i, j));
        }
        return possible_moves;
    }

    @Override
    public void findMoveAndApply(int player, int[] from, int[] to) {
        Map<int[], Map<int[], int[][]>> possible_moves = getPossibleMovesOfPlayer(player);
        for (Map.Entry<int[], Map<int[], int[][]>> entry : possible_moves.entrySet()) {
            for (Map.Entry<int[], int[][]> detail : entry.getValue().entrySet()) {
                if (from[0] == entry.getKey()[0] && from[1] == entry.getKey()[1] && to[0] == detail.getKey()[0] && to[1] == detail.getKey()[1]) {
                    applyMove(from, to, detail.getValue());
                    return;
                }
            }
        }
    }

    @Override
    public Board copyBoardAndMakeMove(int[] from, int[] to, int[][] remove_pieces) {
        Board new_board = copy();
        new_board.applyMove(from, to, remove_pieces);
        return new_board;
    }

    @Override
    public void applyMove(int[] from, int[] to, int[][] remove_pieces) {
        setPieceOnPosition(getPieceOnPosition(from[0], from[1]), to[0], to[1]);
        setPieceOnPosition(NONE, from[0], from[1]);
        for (int[] remove_piece : remove_pieces) setPieceOnPosition(NONE, remove_piece[0], remove_piece[1]);
        // check for new king
        if (getPieceOnPosition(to[0], to[1]) == BLACK && to[0] == board.length-1) setPieceOnPosition(BLACK_KING, to[0], to[1]);
        else if (getPieceOnPosition(to[0], to[1]) == WHITE && to[0] == 0) setPieceOnPosition(WHITE_KING, to[0], to[1]);
    }

    @Override
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board.length;
    }

    @Override
    public Board copy() {
        Board new_board = new Board(board.length);
        // copy board data
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) new_board.getBoard()[i][j] = board[i][j];
        }
        return new_board;
    }

    @Override
    public double evaluate() {
        int white_left = getPiecesLeft(WHITE) + getPiecesLeft(WHITE_KING);
        int black_left = getPiecesLeft(BLACK) + getPiecesLeft(BLACK_KING);
        return black_left - white_left + (getPiecesLeft(BLACK_KING) * 0.5 - getPiecesLeft(WHITE_KING) * 0.5);
    }

    @Override
    public int won() {
        int white_left = getPiecesLeft(WHITE) + getPiecesLeft(WHITE_KING);
        int black_left = getPiecesLeft(BLACK) + getPiecesLeft(BLACK_KING);
        return white_left == 0 ? BLACK_PLAYER : black_left == 0 ? WHITE_PLAYER : NONE_PLAYER;
    }

    private Map<int[], Map<int[], int[][]>> merge(Map<int[], Map<int[], int[][]>> a, Map<int[], Map<int[], int[][]>> b) {
        Map<int[], Map<int[], int[][]>> c = new HashMap<>();
        a.forEach(c::put);
        b.forEach(c::put);
        return c;
    }

    /* SETTER AND GETTER */

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int[][] getBoard() {
        return board;
    }

    /* HELP METHODS TO FIND ALL POSSIBLE MOVES OF A CERTAIN PIECE */

    private Map<int[], Map<int[], int[][]>> getPossibleKingMoves(int player, int row, int col) {
        Map<int[], Map<int[], int[][]>> possible_moves = new HashMap<>();
        possible_moves.put(new int[]{row, col}, getDiagonalKingMoves(player, row, col, new HashMap<>(), -1, -1, new LinkedList<>()));
        possible_moves.put(new int[]{row, col}, getDiagonalKingMoves(player, row, col, new HashMap<>(), -1, 1, new LinkedList<>()));
        possible_moves.put(new int[]{row, col}, getDiagonalKingMoves(player, row, col, new HashMap<>(), 1, -1, new LinkedList<>()));
        possible_moves.put(new int[]{row, col}, getDiagonalKingMoves(player, row, col, new HashMap<>(), 1, 1, new LinkedList<>()));
        return possible_moves;
    }

    private Map<int[], int[][]> getDiagonalKingMoves(int player, int row, int col, Map<int[], int[][]> tmp_moves, int row_dir, int col_dir, List<int[]> tmp_removed) {
        if (getPieceOnPosition(row, col) == ERROR) return tmp_moves;

        if (getPlayerOnPosition(row, col) == NONE_PLAYER) tmp_moves.put(new int[]{row, col}, getArray(tmp_removed));
        else if (getPlayerOnPosition(row, col) == getOppositePlayer(player)) tmp_removed.add(new int[]{row, col});

        return getDiagonalKingMoves(player, row+row_dir, col+col_dir, tmp_moves, row_dir, col_dir, tmp_removed);
    }

    private Map<int[], int[][]> getMapWithoutRemovePieces(int[] to) {
        Map<int[], int[][]> map = new HashMap<>();
        map.put(to, new int[0][2]);
        return map;
    }

    private int[][] getArray(List<int[]> list) {
        int[][] array = new int[list.size()][2];
        AtomicInteger i = new AtomicInteger();
        list.forEach(part_arr -> {
            int[] tmp = new int[]{list.get(i.get())[0], list.get(i.get())[1]};
            array[i.get()] = tmp;
            i.getAndIncrement();
        });
        return array;
    }

    private Map<int[], int[][]> getPossibleJumpMoves(int player, int row, int col, Map<int[], int[][]> tmp_moves, List<int[]> already_removed) {
        int[] left = (player == WHITE_PLAYER) ? new int[]{row-1, col-1} : new int[]{row+1, col-1};
        int[] right = (player == WHITE_PLAYER) ? new int[]{row-1, col+1} : new int[]{row+1, col+1};
        int[] doubleLeft = (player == WHITE_PLAYER) ? new int[]{left[0]-1, left[1]-1} : new int[]{left[0]+1, left[1]-1};
        int[] doubleRight = (player == WHITE_PLAYER) ? new int[]{right[0]-1, right[1]+1} : new int[]{right[0]+1, right[1]+1};

        // left jump
        if (getPlayerOnPosition(left[0], left[1]) == getOppositePlayer(player) && getPlayerOnPosition(doubleLeft[0], doubleLeft[1]) == NONE_PLAYER) {
            // add current jump to tmp_moves
            List<int[]> tmp = new LinkedList<>(already_removed);
            tmp.add(left);
            tmp_moves.put(doubleLeft, tmp.toArray(new int[tmp.size()][2]));
            // look further
            getPossibleJumpMoves(player, doubleLeft[0], doubleLeft[1], tmp_moves, tmp);
        }

        // right jump
        if (getPlayerOnPosition(right[0], right[1]) == getOppositePlayer(player) && getPlayerOnPosition(doubleRight[0], doubleRight[1]) == NONE_PLAYER) {
            // add current jump to tmp_moves
            List<int[]> tmp = new LinkedList<>(already_removed);
            tmp.add(right);
            tmp_moves.put(doubleRight, tmp.toArray(new int[tmp.size()][2]));
            // look further
            getPossibleJumpMoves(player, doubleRight[0], doubleRight[1], tmp_moves, tmp);
        }

        return tmp_moves;
    }
}
