package pl.mikolaj.games.tictactoe;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    private static final int TILE_SIZE = 100;
    public static final int BOARD_SIZE = 5;


    private final boolean playable = true;
    private boolean turnX = true;
    private final Pane root = new Pane();
    private final Tile[][] board = new Tile[BOARD_SIZE][BOARD_SIZE];
    private final List<Combo> combos = new ArrayList<>();

    private Parent createContent() {
        root.setPrefSize(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                Tile tile = new Tile();
                tile.setTranslateX(x * TILE_SIZE);
                tile.setTranslateY(y * TILE_SIZE);
                root.getChildren().add(tile);

                board[y][x] = tile;
            }
        }

        //rows
        for (int y = 0; y < BOARD_SIZE; y++) {
            combos.add(new Combo(board[y]));
        }

        //columns
        for(int x = 0; x < BOARD_SIZE; x++) {
            Tile[] column = new Tile[BOARD_SIZE];
            for (int y = 0; y < BOARD_SIZE; y++) {
                column[y] = board[y][x];
            }
            combos.add(new Combo(column));
        }

        //diagonals
        Tile[] diagonal = new Tile[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            diagonal[i] = board[i][i];
        }
        combos.add(new Combo(diagonal));

        diagonal = new Tile[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            diagonal[i] = board[BOARD_SIZE - 1 - i][i];
        }
        combos.add(new Combo(diagonal));

        return root;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    private void checkState() {
        Optional<Combo> winner = combos
                .stream()
                .filter(Combo::isComplete)
                .findAny();

        if (winner.isPresent()) {
            System.out.println(winner.get().getWinner() + " won!");
            System.exit(0);
        }
    }

    private class Combo {
        private final Tile[] tiles;

        public Combo(Tile... tiles) {
            this.tiles = tiles;
        }

        public boolean isComplete() {
            if (tiles[0].getValue().isEmpty()) {
                return false;
            }

            return Arrays.stream(tiles)
                    .skip(1)
                    .allMatch(tile -> tiles[0].getValue().equals(tile.getValue()));
        }

        public String getWinner() {
            return tiles[0].getValue();
        }
    }

    private class Tile extends StackPane {
        private final Text text = new Text();

        public Tile() {
            Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);
            border.setFill(null);
            border.setStroke(Color.BLACK);

            text.setFont(Font.font(72));

            setAlignment(Pos.CENTER);
            getChildren().addAll(border, text);

            setOnMouseClicked(event -> {
                if (!playable) {
                    return;
                }

                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!turnX) {
                        return;
                    }

                    drawX();
                    turnX = false;
                    checkState();

                } else if (event.getButton() == MouseButton.SECONDARY) {
                    if (turnX) {
                        return;
                    }

                    drawO();
                    turnX = true;
                    checkState();
                }
             });
        }

        public double getCenterX() {
            return getTranslateX() + (double) TILE_SIZE / 2;
        }

        public double getCenterY() {
            return getTranslateY() + (double) TILE_SIZE / 2;
        }

        public String getValue() {
            return text.getText();
        }

        private void drawX() {
            text.setText("X");
        }

        private void drawO() {
            text.setText("O");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
