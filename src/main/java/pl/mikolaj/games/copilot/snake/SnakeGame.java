package pl.mikolaj.games.copilot.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class SnakeGame {
    private final int columns;
    private final int rows;
    private final Random random;
    private final Deque<Cell> snake = new ArrayDeque<>();

    private Direction direction;
    private Cell food;
    private int score;
    private boolean gameOver;
    private boolean boardFilled;

    public SnakeGame(int columns, int rows) {
        this(columns, rows, new Random());
    }

    SnakeGame(int columns, int rows, Random random) {
        if (columns < 3 || (long) columns * rows <= 3) {
            throw new IllegalArgumentException("The board must fit the three-cell snake and at least one food cell.");
        }

        this.columns = columns;
        this.rows = rows;
        this.random = Objects.requireNonNull(random);
        restart();
    }

    public void restart() {
        snake.clear();
        int startX = columns / 2;
        int startY = rows / 2;
        snake.addLast(new Cell(startX, startY));
        snake.addLast(new Cell(Math.floorMod(startX - 1, columns), startY));
        snake.addLast(new Cell(Math.floorMod(startX - 2, columns), startY));
        direction = Direction.RIGHT;
        score = 0;
        gameOver = false;
        boardFilled = false;
        placeFood();
    }

    public void turn(Direction newDirection) {
        Objects.requireNonNull(newDirection);
        if (newDirection != direction.opposite()) {
            direction = newDirection;
        }
    }

    public void tick() {
        if (gameOver) {
            return;
        }

        Cell head = snake.getFirst();
        Cell next = new Cell(
                Math.floorMod(head.x() + direction.dx, columns),
                Math.floorMod(head.y() + direction.dy, rows));
        boolean eating = next.equals(food);
        Cell tail = snake.getLast();

        if (snake.contains(next) && (eating || !next.equals(tail))) {
            gameOver = true;
            return;
        }

        snake.addFirst(next);
        if (eating) {
            score++;
            if (snake.size() == columns * rows) {
                gameOver = true;
                boardFilled = true;
            } else {
                placeFood();
            }
        } else {
            snake.removeLast();
        }
    }

    public List<Cell> getSnake() {
        return List.copyOf(snake);
    }

    public Cell getFood() {
        return food;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isBoardFilled() {
        return boardFilled;
    }

    public enum Direction {
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        LEFT(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        private Direction opposite() {
            return switch (this) {
                case UP -> DOWN;
                case RIGHT -> LEFT;
                case DOWN -> UP;
                case LEFT -> RIGHT;
            };
        }
    }

    public record Cell(int x, int y) {
    }

    private void placeFood() {
        List<Cell> emptyCells = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Cell cell = new Cell(x, y);
                if (!snake.contains(cell)) {
                    emptyCells.add(cell);
                }
            }
        }
        food = emptyCells.get(random.nextInt(emptyCells.size()));
    }
}
