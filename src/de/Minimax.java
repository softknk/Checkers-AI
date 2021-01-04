package de;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Minimax {

    public static BoardEval minimax(Board board, int curr_player, int depth) {
        if (depth == 0 || board.won() != BoardFunction.NONE_PLAYER) return new BoardEval(board.evaluate(), board);

        double maxEval;
        Board best_move = null;

        if (curr_player == BoardFunction.BLACK_PLAYER) {
            maxEval = -Double.MAX_VALUE;
            for (Move move : getMovesWithHighestJumpsFirst(board.getPossibleMovesOfPlayer(curr_player))) {
                Board tmp_move = board.copyBoardAndMakeMove(move.getFrom(), move.getTo(), move.getRemovePieces());
                double evaluation = minimax(tmp_move.copy(), BoardFunction.WHITE_PLAYER,depth-1).getMaxEval();
                maxEval = Math.max(maxEval, evaluation);
                if (maxEval == evaluation) best_move = tmp_move.copy();
            }
        } else {
            maxEval = Double.MAX_VALUE;
            for (Move move : getMovesWithHighestJumpsFirst(board.getPossibleMovesOfPlayer(curr_player))) {
                Board tmp_move = board.copyBoardAndMakeMove(move.getFrom(), move.getTo(), move.getRemovePieces());
                double evaluation = minimax(tmp_move.copy(), BoardFunction.BLACK_PLAYER,depth-1).getMaxEval();
                maxEval = Math.min(maxEval, evaluation);
                if (maxEval == evaluation) best_move = tmp_move.copy();
            }
        }
        return new BoardEval(maxEval, best_move);
    }

    public static List<Move> getMovesAsListWithNJumps(int jumps, Map<int[], Map<int[], int[][]>> moves) {
        List<Move> moves_list = new LinkedList<>();
        for (Map.Entry<int[], Map<int[], int[][]>> entry : moves.entrySet()) {
            for (Map.Entry<int[], int[][]> detail : entry.getValue().entrySet()) {
                if (detail.getValue().length == jumps) moves_list.add(new Move(entry.getKey(), detail.getKey(), detail.getValue()));
            }
        }
        return moves_list;
    }

    public static List<Move> getMovesWithHighestJumpsFirst(Map<int[], Map<int[], int[][]>> moves) {
        List<Move> highest = getMovesAsListWithNJumps(0, moves);
        int tmp_jumps = 1;
        while (getMovesAsListWithNJumps(tmp_jumps, moves).size() != 0) {
            highest = getMovesAsListWithNJumps(tmp_jumps, moves);
            tmp_jumps++;
        }
        return highest;
    }

    public static class BoardEval {

        private final double maxEval;
        private final Board board;

        public BoardEval(double maxEval, Board board) {
            this.maxEval = maxEval;
            this.board = board;
        }

        public double getMaxEval() {
            return maxEval;
        }

        public Board getBoard() {
            return board;
        }
    }

    public static class Move {

        private final int[] from;
        private final int[] to;
        private final int[][] remove_pieces;

        public Move(int[] from, int[] to, int[][] remove_pieces) {
            this.from = new int[]{from[0], from[1]};
            this.to = new int[]{to[0], to[1]};
            if (remove_pieces == null || remove_pieces.length == 0) this.remove_pieces = new int[0][2];
            else {
                int[][] remove_pieces_tmp = new int[remove_pieces.length][2];
                // copy data
                for (int i = 0; i < remove_pieces.length; i++) {
                    if (remove_pieces[i] != null) remove_pieces_tmp[i] = new int[]{remove_pieces[i][0], remove_pieces[i][1]};
                }
                this.remove_pieces = remove_pieces_tmp;
            }
        }

        public int[] getFrom() {
            return from;
        }

        public int[] getTo() {
            return to;
        }

        public int[][] getRemovePieces() {
            return remove_pieces;
        }
    }
}
