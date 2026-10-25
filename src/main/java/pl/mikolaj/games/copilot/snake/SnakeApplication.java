package pl.mikolaj.games.copilot.snake;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class SnakeApplication extends Application {
    private static final int CELL_SIZE = 20;
    private static final int COLUMNS = 30;
    private static final int ROWS = 22;
    private static final Color BOARD_COLOR = Color.web("#1f2937");
    private static final Color SNAKE_COLOR = Color.web("#34d399");
    private static final Color FOOD_COLOR = Color.web("#fb7185");

    private final SnakeGame game = new SnakeGame(COLUMNS, ROWS);
    private final Canvas canvas = new Canvas(COLUMNS * CELL_SIZE, ROWS * CELL_SIZE);
    private final javafx.scene.control.Label scoreLabel = new javafx.scene.control.Label();
    private final javafx.scene.control.Label helpLabel = new javafx.scene.control.Label(
            "Move: arrows / WASD     Restart: Enter or Space");

    @Override
    public void start(Stage stage) {
        canvas.setFocusTraversable(true);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        helpLabel.setTextFill(Color.web("#9ca3af"));
        helpLabel.setFont(Font.font("System", 13));

        HBox header = new HBox(scoreLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 12, 0));

        VBox footer = new VBox(helpLabel);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 0, 0, 0));

        BorderPane root = new BorderPane(canvas, header, null, footer, null);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #111827;");

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> handleKey(event.getCode()));
        stage.setTitle("Snake");
        stage.setResizable(false);
        stage.setScene(scene);

        Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(125), event -> {
            game.tick();
            render();
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();

        render();
        stage.show();
        canvas.requestFocus();
    }

    private void handleKey(KeyCode keyCode) {
        if (game.isGameOver()) {
            if (keyCode == KeyCode.ENTER || keyCode == KeyCode.SPACE) {
                game.restart();
                render();
            }
            return;
        }

        SnakeGame.Direction direction = switch (keyCode) {
            case UP, W -> SnakeGame.Direction.UP;
            case RIGHT, D -> SnakeGame.Direction.RIGHT;
            case DOWN, S -> SnakeGame.Direction.DOWN;
            case LEFT, A -> SnakeGame.Direction.LEFT;
            default -> null;
        };
        if (direction != null) {
            game.turn(direction);
        }
    }

    private void render() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(BOARD_COLOR);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        graphics.setStroke(Color.web("#374151"));
        graphics.setLineWidth(0.5);
        for (int x = 0; x <= COLUMNS; x++) {
            graphics.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, canvas.getHeight());
        }
        for (int y = 0; y <= ROWS; y++) {
            graphics.strokeLine(0, y * CELL_SIZE, canvas.getWidth(), y * CELL_SIZE);
        }

        SnakeGame.Cell food = game.getFood();
        graphics.setFill(FOOD_COLOR);
        graphics.fillOval(food.x() * CELL_SIZE + 3, food.y() * CELL_SIZE + 3,
                CELL_SIZE - 6, CELL_SIZE - 6);

        var snake = game.getSnake();
        for (int i = 0; i < snake.size(); i++) {
            SnakeGame.Cell cell = snake.get(i);
            graphics.setFill(i == 0 ? Color.web("#a7f3d0") : SNAKE_COLOR);
            graphics.fillRoundRect(cell.x() * CELL_SIZE + 1, cell.y() * CELL_SIZE + 1,
                    CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);
        }

        scoreLabel.setText("SCORE  " + game.getScore());
        if (game.isGameOver()) {
            graphics.setFill(Color.rgb(17, 24, 39, 0.78));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphics.setFill(Color.WHITE);
            graphics.setFont(Font.font("System", FontWeight.BOLD, 30));
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.fillText(game.isBoardFilled() ? "Board cleared!" : "Game over",
                    canvas.getWidth() / 2, canvas.getHeight() / 2 - 8);
            graphics.setFont(Font.font("System", 16));
            graphics.fillText("Press Enter or Space to play again",
                    canvas.getWidth() / 2, canvas.getHeight() / 2 + 24);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
