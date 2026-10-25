package pl.mikolaj.games.copilot.snake;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeGameTest {
    @Test
    void wrapsAtBoardEdges() {
        SnakeGame game = new SnakeGame(6, 5, new Random(1));

        game.tick();
        game.tick();
        game.tick();

        assertEquals(new SnakeGame.Cell(0, 2), game.getSnake().getFirst());
        assertFalse(game.isGameOver());
    }

    @Test
    void rejectsImmediateReverseDirection() {
        SnakeGame game = new SnakeGame(6, 5, new Random(1));

        game.turn(SnakeGame.Direction.LEFT);
        game.tick();

        assertEquals(new SnakeGame.Cell(4, 2), game.getSnake().getFirst());
        assertFalse(game.isGameOver());
    }

    @Test
    void eatingFoodIncreasesScoreAndLength() {
        SnakeGame game = new SnakeGame(6, 5, new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });

        game.turn(SnakeGame.Direction.UP);
        game.tick();
        game.turn(SnakeGame.Direction.LEFT);
        game.tick();
        game.tick();
        game.tick();
        game.turn(SnakeGame.Direction.UP);
        game.tick();

        assertEquals(1, game.getScore());
        assertEquals(4, game.getSnake().size());
        assertNotEquals(game.getFood(), game.getSnake().getFirst());
    }

    @Test
    void hittingOwnBodyEndsTheRound() {
        SnakeGame game = new SnakeGame(6, 5, new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });

        game.turn(SnakeGame.Direction.UP);
        game.tick();
        game.turn(SnakeGame.Direction.LEFT);
        game.tick();
        game.tick();
        game.tick();
        game.turn(SnakeGame.Direction.UP);
        game.tick();
        game.turn(SnakeGame.Direction.RIGHT);
        game.tick();
        game.turn(SnakeGame.Direction.DOWN);
        game.tick();

        assertTrue(game.isGameOver());
        assertFalse(game.isBoardFilled());
    }

    @Test
    void restartResetsScoreAndSnake() {
        SnakeGame game = new SnakeGame(6, 5, new Random(1));
        game.tick();
        game.restart();

        assertEquals(0, game.getScore());
        assertEquals(3, game.getSnake().size());
        assertEquals(new SnakeGame.Cell(3, 2), game.getSnake().getFirst());
        assertFalse(game.isGameOver());
        assertTrue(game.getFood() != null);
    }
}
