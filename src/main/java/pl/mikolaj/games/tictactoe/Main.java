package pl.mikolaj.games.tictactoe;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Main extends Application {

    private static final int TILE_SIZE = 100;
    private static final int BOARD_SIZE = 3;

    private boolean playable = true;
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
        combos.add(createDiagonalCombo(true));
        combos.add(createDiagonalCombo(false));

        return root;
    }

    private Combo createDiagonalCombo(boolean mainDiagonal) {
        Tile[] diagonal = new Tile[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            diagonal[i] =  mainDiagonal ? board[i][i] : board[BOARD_SIZE - 1 - i][i];
        }
        return new Combo(diagonal);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    private void checkState() {
        combos.stream()
                .filter(Combo::isComplete)
                .findFirst()
                .ifPresentOrElse(this::handleWin, this::checkForDraw);
    }
    
    private void handleWin(Combo winner) {
        playable = false;
        drawWinningLine(winner);
        System.out.println(winner.getWinnerSymbol() + " won!");
    }
    
    private void checkForDraw() {
        if (isDraw()) {
            playable = false;
            System.out.println("It's a Draw!");
        }
    }

    private boolean isDraw() {
        return getEmptyTiles().isEmpty();
    }

    private void computerMove() {
        if (!playable) {
            return;
        }
        Tile tile = findRandomFreeTile();
        tile.drawO();
        checkState();
        turnX = true;
    }

    private Tile findRandomFreeTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        Random random =  new Random();
        int next = random.nextInt(emptyTiles.size());
        return emptyTiles.get(next);
    }
    
    private List<Tile> getEmptyTiles() {
        return Arrays.stream(board)
                .flatMap(Arrays::stream)
                .filter(Tile::isEmpty)
                .toList();
    }

    private void drawWinningLine(Combo combo) {
        Line line = new Line();
        line.setStartX(combo.tiles[0].getCenterX());
        line.setStartY(combo.tiles[0].getCenterY());
        line.setEndX(combo.tiles[0].getCenterX());
        line.setEndY(combo.tiles[0].getCenterY());

        root.getChildren().add(line);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames()
                .add(new KeyFrame(
                        Duration.seconds(1),
                        new KeyValue(line.endXProperty(), combo.tiles[BOARD_SIZE - 1].getCenterX()),
                        new KeyValue(line.endYProperty(), combo.tiles[BOARD_SIZE - 1].getCenterY()))
                );
        timeline.play();
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

        public String getWinnerSymbol() {
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

                if (turnX && isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    drawX();
                    checkState();
                    turnX = false;
                    computerMove();
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

        private boolean isEmpty() {
            return StringUtils.isEmpty(text.getText());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
