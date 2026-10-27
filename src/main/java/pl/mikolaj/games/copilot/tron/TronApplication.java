package pl.mikolaj.games.copilot.tron;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class TronApplication extends Application {
    private static final int COLUMNS = 140;
    private static final int ROWS = 100;
    private static final Color BOARD_COLOR = Color.web("#111827");
    private static final Color HUMAN_HEAD = Color.web("#a7f3d0");
    private static final Color HUMAN_TRAIL = Color.web("#34d399");
    private static final Color COMPUTER_HEAD = Color.web("#bfdbfe");
    private static final Color COMPUTER_TRAIL = Color.web("#60a5fa");
    private static final Color SECOND_PLAYER_HEAD = Color.web("#fca5a5");
    private static final Color SECOND_PLAYER_TRAIL = Color.web("#f87171");

    private int cellSize = 10;
    private TronGame game;
    private Canvas canvas;
    private Timeline loop;
    private Stage stage;
    private final Label statusLabel = new Label();
    private final Label helpLabel = new Label();

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Tron");
        stage.setResizable(true);
        stage.setScene(createMenuScene());
        stage.show();
    }

    private Scene createMenuScene() {
        Label title = new Label("Tron");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 42));

        Label subtitle = new Label("Choose a game mode");
        subtitle.setTextFill(Color.web("#cbd5e1"));
        subtitle.setFont(Font.font("System", 20));

        Button singlePlayerButton = new Button("Single Player");
        singlePlayerButton.setPrefWidth(220);
        singlePlayerButton.setPrefHeight(48);
        singlePlayerButton.setOnAction(event -> startGame(TronGame.GameMode.SINGLE_PLAYER));

        Button multiplayerButton = new Button("Multiplayer");
        multiplayerButton.setPrefWidth(220);
        multiplayerButton.setPrefHeight(48);
        multiplayerButton.setOnAction(event -> startGame(TronGame.GameMode.MULTIPLAYER));

        VBox menu = new VBox(20, title, subtitle, singlePlayerButton, multiplayerButton);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));
        menu.setStyle("-fx-background-color: #0f172a;");

        Scene menuScene = new Scene(menu, 500, 400);
        menuScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                returnToMenu();
            }
        });
        return menuScene;
    }

    private void returnToMenu() {
        if (loop != null) {
            loop.stop();
            loop = null;
        }
        game = null;
        canvas = null;
        stage.setTitle("Tron");
        stage.setScene(createMenuScene());
    }

    private void startGame(TronGame.GameMode mode) {
        game = new TronGame(COLUMNS, ROWS);
        game.setMode(mode);

        canvas = new Canvas(COLUMNS * cellSize, ROWS * cellSize);
        configureBoardForScreen();
        canvas.setFocusTraversable(true);
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        helpLabel.setTextFill(Color.web("#9ca3af"));
        helpLabel.setFont(Font.font("System", 13));
        helpLabel.setText(mode == TronGame.GameMode.SINGLE_PLAYER
                ? "Move: arrows / WASD     Restart: Enter or Space"
                : "P1: arrows / WASD     P2: IJKL     Restart: Enter or Space");

        VBox header = new VBox(statusLabel, helpLabel);
        header.setAlignment(Pos.CENTER);
        header.setSpacing(10);
        header.setPadding(new Insets(0, 0, 12, 0));

        BorderPane root = new BorderPane(canvas, header, null, null, null);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> handleKey(event.getCode()));
        stage.setTitle(mode == TronGame.GameMode.SINGLE_PLAYER ? "Tron - Single Player" : "Tron - Multiplayer");
        stage.setResizable(true);
        stage.setScene(scene);

        if (loop != null) {
            loop.stop();
        }
        loop = new Timeline(new KeyFrame(Duration.millis(75), event -> {
            game.tick();
            render();
        }));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();

        render();
        canvas.requestFocus();
    }

    private void configureBoardForScreen() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double usableWidth = bounds.getWidth() - 120;
        double usableHeight = bounds.getHeight() - 150;
        this.cellSize = (int) Math.max(4, Math.floor(Math.min(usableWidth / COLUMNS, usableHeight / ROWS)));
        if (canvas != null) {
            canvas.setWidth(COLUMNS * cellSize);
            canvas.setHeight(ROWS * cellSize);
        }
    }

    private void handleKey(KeyCode keyCode) {
        if (game == null) {
            return;
        }

        if (keyCode == KeyCode.ESCAPE) {
            returnToMenu();
            return;
        }

        if (game.isGameOver()) {
            if (keyCode == KeyCode.ENTER || keyCode == KeyCode.SPACE) {
                game.restart();
                render();
            }
            return;
        }

        if (game.getMode() == TronGame.GameMode.SINGLE_PLAYER) {
            TronGame.Direction direction = switch (keyCode) {
                case UP, W -> TronGame.Direction.UP;
                case RIGHT, D -> TronGame.Direction.RIGHT;
                case DOWN, S -> TronGame.Direction.DOWN;
                case LEFT, A -> TronGame.Direction.LEFT;
                default -> null;
            };
            if (direction != null) {
                game.turn(direction);
            }
            return;
        }

        if (keyCode == KeyCode.ENTER || keyCode == KeyCode.SPACE) {
            return;
        }

        if (isPlayerOneKey(keyCode)) {
            game.turn(mapToDirection(keyCode));
            return;
        }

        if (isPlayerTwoKey(keyCode)) {
            game.turnSecondPlayer(mapSecondPlayerDirection(keyCode));
        }
    }

    private boolean isPlayerOneKey(KeyCode keyCode) {
        return switch (keyCode) {
            case UP, W, RIGHT, D, DOWN, S, LEFT, A -> true;
            default -> false;
        };
    }

    private boolean isPlayerTwoKey(KeyCode keyCode) {
        return switch (keyCode) {
            case I, J, K, L -> true;
            default -> false;
        };
    }

    private TronGame.Direction mapToDirection(KeyCode keyCode) {
        return switch (keyCode) {
            case UP, W -> TronGame.Direction.UP;
            case RIGHT, D -> TronGame.Direction.RIGHT;
            case DOWN, S -> TronGame.Direction.DOWN;
            case LEFT, A -> TronGame.Direction.LEFT;
            default -> throw new IllegalArgumentException("Unsupported key: " + keyCode);
        };
    }

    private TronGame.Direction mapSecondPlayerDirection(KeyCode keyCode) {
        return switch (keyCode) {
            case I -> TronGame.Direction.UP;
            case L -> TronGame.Direction.RIGHT;
            case K -> TronGame.Direction.DOWN;
            case J -> TronGame.Direction.LEFT;
            default -> throw new IllegalArgumentException("Unsupported key: " + keyCode);
        };
    }

    private void render() {
        if (canvas == null || game == null) {
            return;
        }

        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(BOARD_COLOR);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        graphics.setStroke(Color.web("#1f2937"));
        graphics.setLineWidth(0.5);
        for (int x = 0; x <= COLUMNS; x++) {
            graphics.strokeLine(x * cellSize, 0, x * cellSize, canvas.getHeight());
        }
        for (int y = 0; y <= ROWS; y++) {
            graphics.strokeLine(0, y * cellSize, canvas.getWidth(), y * cellSize);
        }

        drawTrail(graphics, game.getHumanTrail(), HUMAN_TRAIL, HUMAN_HEAD);
        if (game.getMode() == TronGame.GameMode.SINGLE_PLAYER) {
            drawTrail(graphics, game.getComputerTrail(), COMPUTER_TRAIL, COMPUTER_HEAD);
        } else {
            drawTrail(graphics, game.getSecondPlayerTrail(), SECOND_PLAYER_TRAIL, SECOND_PLAYER_HEAD);
        }

        String modeText = game.getMode() == TronGame.GameMode.SINGLE_PLAYER ? "Human vs CPU" : "Player 1 vs Player 2";
        statusLabel.setText(game.isGameOver()
                ? (game.isDraw() ? "Draw! Press Enter to retry" : switch (game.getWinner()) {
                    case HUMAN -> "Player 1 wins!  Press Enter to retry";
                    case COMPUTER -> "Computer wins!  Press Enter to retry";
                    case SECOND_PLAYER -> "Player 2 wins!  Press Enter to retry";
                })
                : modeText);

        if (game.isGameOver()) {
            graphics.setFill(Color.rgb(15, 23, 42, 0.72));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphics.setFill(Color.WHITE);
            graphics.setFont(Font.font("System", FontWeight.BOLD, 30));
            graphics.setTextAlign(TextAlignment.CENTER);
            if (game.isDraw()) {
                graphics.fillText("Draw", canvas.getWidth() / 2, canvas.getHeight() / 2 - 10);
            } else {
                String winnerText = switch (game.getWinner()) {
                    case HUMAN -> "Player 1 won!";
                    case COMPUTER -> "CPU won!";
                    case SECOND_PLAYER -> "Player 2 won!";
                };
                graphics.fillText(winnerText, canvas.getWidth() / 2, canvas.getHeight() / 2 - 10);
            }
            graphics.setFont(Font.font("System", 16));
            graphics.fillText("Press Enter or Space to play again",
                    canvas.getWidth() / 2, canvas.getHeight() / 2 + 24);
        }
    }

    private void drawTrail(GraphicsContext graphics, java.util.List<TronGame.Cell> cells,
                           Color trailColor, Color headColor) {
        for (int i = 0; i < cells.size(); i++) {
            TronGame.Cell cell = cells.get(i);
            graphics.setFill(i == 0 ? headColor : trailColor);
            graphics.fillRoundRect(cell.x() * cellSize + 1, cell.y() * cellSize + 1,
                    cellSize - 2, cellSize - 2, 8, 8);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
