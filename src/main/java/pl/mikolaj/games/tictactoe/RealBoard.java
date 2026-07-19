package pl.mikolaj.games.tictactoe;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Random;

public class RealBoard extends AbstractBoard<RealBoard.RealTile> {
    private Pane root;
    private boolean playable = true;
    private boolean turnX = true;

    protected void init(Pane root) {
        this.root = root;
        root.setPrefSize(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);

        super.init(RealTile.class, (y, x) -> {
            RealTile realTile = new RealTile();
            realTile.setTranslateX(x * TILE_SIZE);
            realTile.setTranslateY(y * TILE_SIZE);
            root.getChildren().add(realTile);
            return realTile;
        });
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
        if (isDraw(tiles)) {
            playable = false;
            System.out.println("It's a Draw!");
        }
    }

    private void computerMove() {
        if (!playable) {
            return;
        }
        RealTile tile = findRandomFreeTile();
        tile.setValue(COMPUTER_SYMBOL);
        checkState();
        turnX = true;
    }

    private RealTile findRandomFreeTile() {
        List<RealTile> emptyTiles = getEmptyTiles(tiles);
        Random random = new Random();
        int next = random.nextInt(emptyTiles.size());
        return emptyTiles.get(next);
    }

    private void drawWinningLine(Combo combo) {
        Line line = new Line();
        line.setStartX(combo.tiles.getFirst().getCenterX());
        line.setStartY(combo.tiles.getFirst().getCenterY());
        line.setEndX(combo.tiles.getFirst().getCenterX());
        line.setEndY(combo.tiles.getFirst().getCenterY());

        root.getChildren().add(line);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames()
                .add(new KeyFrame(
                        Duration.seconds(1),
                        new KeyValue(line.endXProperty(), combo.tiles.getLast().getCenterX()),
                        new KeyValue(line.endYProperty(), combo.tiles.getLast().getCenterY()))
                );
        timeline.play();
    }

    protected class RealTile extends StackPane implements AbstractTile {

        private final Text text = new Text();

        public RealTile() {
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
                    setValue(PLAYER_SYMBOL);
                    checkState();
                    turnX = false;
                    computerMove();
                }
            });
        }

        @Override
        public String getValue() {
            return text.getText();
        }

        @Override
        public void setValue(String value) {
            text.setText(value);
        }

        @Override
        public boolean isEmpty() {
            return StringUtils.isEmpty(text.getText());
        }

        public double getCenterX() {
            return getTranslateX() + (double) TILE_SIZE / 2;
        }

        public double getCenterY() {
            return getTranslateY() + (double) TILE_SIZE / 2;
        }
    }
}
