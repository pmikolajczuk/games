package pl.mikolaj.games.copilot.tron;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class TronGame {
    private static final int AGGRESSION_DISTANCE_WEIGHT = 500;

    private final int columns;
    private final int rows;
    private final Deque<Cell> humanTrail = new ArrayDeque<>();
    private final Deque<Cell> computerTrail = new ArrayDeque<>();
    private final Deque<Cell> secondPlayerTrail = new ArrayDeque<>();

    private Direction humanDirection;
    private Direction computerDirection;
    private Direction secondPlayerDirection;
    private GameMode mode = GameMode.SINGLE_PLAYER;
    private boolean gameOver;
    private Player winner;
    private boolean draw;

    public TronGame(int columns, int rows) {
        if (columns < 10 || rows < 8) {
            throw new IllegalArgumentException("The board must be at least 10x8 to support a simple Tron match.");
        }
        this.columns = columns;
        this.rows = rows;
        restart();
    }

    public void setMode(GameMode mode) {
        this.mode = Objects.requireNonNull(mode);
        restart();
    }

    public GameMode getMode() {
        return mode;
    }

    public void restart() {
        humanTrail.clear();
        computerTrail.clear();
        secondPlayerTrail.clear();

        int middleY = rows / 2;
        humanTrail.addLast(new Cell(2, middleY));
        humanTrail.addLast(new Cell(1, middleY));
        humanTrail.addLast(new Cell(0, middleY));

        computerTrail.addLast(new Cell(columns - 3, middleY));
        computerTrail.addLast(new Cell(columns - 2, middleY));
        computerTrail.addLast(new Cell(columns - 1, middleY));

        if (mode == GameMode.MULTIPLAYER) {
            secondPlayerTrail.addLast(new Cell(columns - 6, middleY));
            secondPlayerTrail.addLast(new Cell(columns - 5, middleY));
            secondPlayerTrail.addLast(new Cell(columns - 4, middleY));
            secondPlayerDirection = Direction.LEFT;
        }

        humanDirection = Direction.RIGHT;
        computerDirection = Direction.LEFT;
        gameOver = false;
        winner = null;
        draw = false;
    }

    public void turn(Direction newDirection) {
        Objects.requireNonNull(newDirection);
        if (newDirection != humanDirection.opposite()) {
            humanDirection = newDirection;
        }
    }

    public void turnSecondPlayer(Direction newDirection) {
        Objects.requireNonNull(newDirection);
        if (newDirection != secondPlayerDirection.opposite()) {
            secondPlayerDirection = newDirection;
        }
    }

    public void tick() {
        if (gameOver) {
            return;
        }

        if (mode == GameMode.SINGLE_PLAYER) {
            computerDirection = chooseComputerDirection();
            Cell nextHuman = nextCell(humanTrail.getFirst(), humanDirection);
            Cell nextComputer = nextCell(computerTrail.getFirst(), computerDirection);

            if (nextHuman.equals(nextComputer)) {
                gameOver = true;
                draw = true;
                return;
            }

            boolean humanCrash = collides(nextHuman, humanTrail, computerTrail);
            boolean computerCrash = collides(nextComputer, computerTrail, humanTrail);

            if (humanCrash && computerCrash) {
                gameOver = true;
                draw = true;
                return;
            }
            if (humanCrash) {
                gameOver = true;
                winner = Player.COMPUTER;
                return;
            }
            if (computerCrash) {
                gameOver = true;
                winner = Player.HUMAN;
                return;
            }

            humanTrail.addFirst(nextHuman);
            computerTrail.addFirst(nextComputer);
            return;
        }

        Cell nextHuman = nextCell(humanTrail.getFirst(), humanDirection);
        Cell nextSecondPlayer = nextCell(secondPlayerTrail.getFirst(), secondPlayerDirection);

        if (nextHuman.equals(nextSecondPlayer)) {
            gameOver = true;
            draw = true;
            return;
        }

        boolean humanCrash = collides(nextHuman, humanTrail, secondPlayerTrail) || nextHuman.equals(nextSecondPlayer);
        boolean secondPlayerCrash = collides(nextSecondPlayer, secondPlayerTrail, humanTrail) || nextSecondPlayer.equals(nextHuman);

        if (humanCrash && secondPlayerCrash) {
            gameOver = true;
            draw = true;
            return;
        }
        if (humanCrash) {
            gameOver = true;
            winner = Player.SECOND_PLAYER;
            return;
        }
        if (secondPlayerCrash) {
            gameOver = true;
            winner = Player.HUMAN;
            return;
        }

        humanTrail.addFirst(nextHuman);
        secondPlayerTrail.addFirst(nextSecondPlayer);
    }

    public List<Cell> getHumanTrail() {
        return List.copyOf(humanTrail);
    }

    public List<Cell> getComputerTrail() {
        return List.copyOf(computerTrail);
    }

    public List<Cell> getSecondPlayerTrail() {
        return List.copyOf(secondPlayerTrail);
    }

    public Direction getHumanDirection() {
        return humanDirection;
    }

    public Direction getComputerDirection() {
        return computerDirection;
    }

    public Direction getSecondPlayerDirection() {
        return secondPlayerDirection;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public boolean isDraw() {
        return draw;
    }

    private boolean collides(Cell next, Deque<Cell> self, Deque<Cell> other) {
        if (next.x() < 0 || next.x() >= columns || next.y() < 0 || next.y() >= rows) {
            return true;
        }
        return self.contains(next) || other.contains(next);
    }

    private Direction chooseComputerDirection() {
        Direction best = computerDirection;
        int bestScore = Integer.MIN_VALUE;
        List<Cell> humanMoves = getPossibleMoves(humanTrail.getFirst(), humanDirection, humanTrail, computerTrail);

        for (Direction direction : Direction.values()) {
            if (direction == computerDirection.opposite()) {
                continue;
            }
            Cell proposed = nextCell(computerTrail.getFirst(), direction);
            if (collides(proposed, computerTrail, humanTrail) || humanMoves.contains(proposed)) {
                continue;
            }

            int score = evaluateMove(proposed, humanMoves);
            if (score > bestScore || (score == bestScore && direction == computerDirection)) {
                bestScore = score;
                best = direction;
            }
        }

        if (bestScore == Integer.MIN_VALUE) {
            return computerDirection;
        }

        return best;
    }

    private int evaluateMove(Cell proposed, List<Cell> humanMoves) {
        boolean[][] blocked = getBlockedCells(proposed);
        int[][] computerDistances = getDistanceMap(List.of(proposed), blocked);
        int[][] humanDistances = getDistanceMap(humanMoves, blocked);

        int reachableCells = 0;
        int territoryScore = 0;
        int closestHumanMove = Integer.MAX_VALUE;
        for (Cell humanMove : humanMoves) {
            int distance = computerDistances[humanMove.y()][humanMove.x()];
            if (distance >= 0) {
                closestHumanMove = Math.min(closestHumanMove, distance);
            }
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                int computerDistance = computerDistances[y][x];
                if (computerDistance < 0) {
                    continue;
                }
                reachableCells++;
                int humanDistance = humanDistances[y][x];
                if (humanDistance < 0 || computerDistance < humanDistance) {
                    territoryScore += 2;
                } else if (computerDistance == humanDistance) {
                    territoryScore++;
                }
            }
        }

        int openMoves = countAvailableMoves(proposed, blocked);
        int pressureScore = closestHumanMove == Integer.MAX_VALUE
                ? 0
                : -closestHumanMove * AGGRESSION_DISTANCE_WEIGHT;
        return territoryScore * 100 + reachableCells * 10 + openMoves * 25 + pressureScore;
    }

    private List<Cell> getPossibleMoves(Cell head, Direction direction, Deque<Cell> self, Deque<Cell> other) {
        List<Cell> moves = new ArrayList<>(3);
        for (Direction candidateDirection : Direction.values()) {
            if (candidateDirection == direction.opposite()) {
                continue;
            }
            Cell candidate = nextCell(head, candidateDirection);
            if (!collides(candidate, self, other)) {
                moves.add(candidate);
            }
        }
        return moves;
    }

    private boolean[][] getBlockedCells(Cell proposed) {
        boolean[][] blocked = new boolean[rows][columns];
        for (Cell cell : humanTrail) {
            blocked[cell.y()][cell.x()] = true;
        }
        for (Cell cell : computerTrail) {
            blocked[cell.y()][cell.x()] = true;
        }
        blocked[proposed.y()][proposed.x()] = true;
        return blocked;
    }

    private int[][] getDistanceMap(List<Cell> starts, boolean[][] blocked) {
        int[][] distances = new int[rows][columns];
        for (int[] row : distances) {
            Arrays.fill(row, -1);
        }

        int[] queue = new int[columns * rows];
        int queueStart = 0;
        int queueEnd = 0;
        for (Cell start : starts) {
            if (distances[start.y()][start.x()] == -1) {
                distances[start.y()][start.x()] = 0;
                queue[queueEnd++] = start.y() * columns + start.x();
            }
        }

        while (queueStart < queueEnd) {
            int current = queue[queueStart++];
            int x = current % columns;
            int y = current / columns;
            for (Direction direction : Direction.values()) {
                int nextX = x + direction.dx;
                int nextY = y + direction.dy;
                if (nextX < 0 || nextX >= columns || nextY < 0 || nextY >= rows
                        || blocked[nextY][nextX] || distances[nextY][nextX] >= 0) {
                    continue;
                }
                distances[nextY][nextX] = distances[y][x] + 1;
                queue[queueEnd++] = nextY * columns + nextX;
            }
        }
        return distances;
    }

    private int countAvailableMoves(Cell origin, boolean[][] blocked) {
        int safeMoves = 0;
        for (Direction direction : Direction.values()) {
            int x = origin.x() + direction.dx;
            int y = origin.y() + direction.dy;
            if (x >= 0 && x < columns && y >= 0 && y < rows && !blocked[y][x]) {
                safeMoves++;
            }
        }
        return safeMoves;
    }

    private Cell nextCell(Cell current, Direction direction) {
        return new Cell(current.x() + direction.dx, current.y() + direction.dy);
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

    public enum Player {
        HUMAN,
        COMPUTER,
        SECOND_PLAYER
    }

    public enum GameMode {
        SINGLE_PLAYER,
        MULTIPLAYER
    }

    public record Cell(int x, int y) {
    }
}
