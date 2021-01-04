package de;

public class ViewModel {

    private Board board;
    private int curr_player;

    public ViewModel(Board board) {
        this.board = board;
        curr_player = BoardFunction.WHITE_PLAYER;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getCurrPlayer() {
        return curr_player;
    }

    public void changeCurrPlayer() {
        curr_player = (curr_player == BoardFunction.WHITE_PLAYER) ? BoardFunction.BLACK_PLAYER : BoardFunction.WHITE_PLAYER;
    }
}
