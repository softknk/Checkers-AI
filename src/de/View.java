package de;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class View extends Application implements EventHandler<ActionEvent> {

    private Button[][] buttons;

    // make move
    private Button selected_button;
    private boolean selected;

    private static ViewModel viewModel;

    @Override
    public void start(Stage stage) {
        viewModel = new ViewModel(new Board(8));
        viewModel.getBoard().initBoard();

        int HEIGHT = viewModel.getBoard().getBoard().length * 64;
        int WIDTH = viewModel.getBoard().getBoard().length * 64;

        GridPane grid = new GridPane();

        grid.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Board new_board = Minimax.minimax(viewModel.getBoard(), BoardFunction.BLACK_PLAYER, 7).getBoard();
                viewModel.setBoard(new_board.copy());
                redraw();
                if (viewModel.getBoard().won() != BoardFunction.NONE_PLAYER) System.out.println("Game over!");
            } else if (event.getCode() == KeyCode.R) {
                selected = false;
            }
        });

        Scene scene = new Scene(grid, WIDTH - 10, HEIGHT - 10);
        buttons = new Button[viewModel.getBoard().getBoard().length][viewModel.getBoard().getBoard().length];

        boolean blackPlace = false;
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                buttons[i][j] = new Button();
                if (blackPlace) buttons[i][j].setStyle("-fx-font-size: 26px; -fx-background-color: #955f22; -fx-border-weight: 1px; -fx-border-color: white;");
                else buttons[i][j].setStyle("-fx-font-size: 26px; -fx-background-color: #e3c58c; -fx-border-weight: 1px; -fx-border-color: white;");
                buttons[i][j].setPrefSize((double) WIDTH / viewModel.getBoard().getBoard().length, (double) HEIGHT / viewModel.getBoard().getBoard().length);
                buttons[i][j].setOnAction(this);
                grid.add(buttons[i][j], j, i);
                blackPlace = !blackPlace;
            }
            blackPlace = !blackPlace;
        }

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Checkers");
        stage.getIcons().add(new Image(View.class.getResourceAsStream("/res/favicon.png")));
        stage.show();

        redraw();
    }

    private int[] getPositionOfButton(Button button) {
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons.length; j++) if (buttons[i][j] == button) return new int[]{i, j};
        }
        return null;
    }

    @Override
    public void handle(ActionEvent event) {
        if (viewModel.getCurrPlayer() == BoardFunction.WHITE_PLAYER) {
            if (!selected) {
                selected_button = (Button) event.getSource();
            } else {
                Button to_button = (Button) event.getSource();
                int[] from = getPositionOfButton(selected_button), to = getPositionOfButton(to_button);
                viewModel.getBoard().findMoveAndApply(viewModel.getCurrPlayer(), from, to);
                redraw();
                if (viewModel.getBoard().won() != BoardFunction.NONE_PLAYER) System.out.println("Game over!");
            }
            selected = !selected;
        }
    }

    public void redraw() {
        // clear all
        for (int i = 0; i < buttons.length; i++) for (int j = 0; j < buttons.length; j++) drawNone(i, j);
        // draw board
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons.length; j++) {
                switch (viewModel.getBoard().getPieceOnPosition(i, j)) {
                    case Board.WHITE: drawWhite(i, j); break;
                    case Board.BLACK: drawBlack(i, j); break;
                    case Board.WHITE_KING: drawWhiteKing(i, j); break;
                    case Board.BLACK_KING: drawBlackKing(i, j); break;
                    default: drawNone(i, j); break;
                }
            }
        }
    }

    private void drawNone(int row, int col) {
        buttons[row][col].setGraphic(null);
    }

    private void drawWhite(int row, int col) {
        buttons[row][col].setGraphic(new ImageView(new Image(View.class.getResourceAsStream("/res/white.png"), 24, 24, true, true)));
    }

    private void drawBlack(int row, int col) {
        buttons[row][col].setGraphic(new ImageView(new Image(View.class.getResourceAsStream("/res/black.png"), 24, 24, true, true)));
    }

    private void drawWhiteKing(int row, int col) {
        buttons[row][col].setGraphic(new ImageView(new Image(View.class.getResourceAsStream("/res/white_checkers.png"), 24, 24, true, true)));
    }

    private void drawBlackKing(int row, int col) {
        buttons[row][col].setGraphic(new ImageView(new Image(View.class.getResourceAsStream("/res/black_checkers.png"), 24, 24, true, true)));
    }
}

