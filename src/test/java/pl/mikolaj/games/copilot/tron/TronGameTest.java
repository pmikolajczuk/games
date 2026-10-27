package pl.mikolaj.games.copilot.tron;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TronGameTest {
    @Test
    void multiplayerStartsWithTwoValidPlayers() {
        TronGame game = new TronGame(20, 10);
        game.setMode(TronGame.GameMode.MULTIPLAYER);

        assertEquals(3, game.getHumanTrail().size());
        assertEquals(3, game.getSecondPlayerTrail().size());
        assertEquals(TronGame.Direction.RIGHT, game.getHumanDirection());
        assertEquals(TronGame.Direction.LEFT, game.getSecondPlayerDirection());
        assertFalse(game.isGameOver());
        assertNull(game.getWinner());
    }

    @Test
    void multiplayerDoesNotAwardWinnerBeforeAnyInput() {
        TronGame game = new TronGame(20, 10);
        game.setMode(TronGame.GameMode.MULTIPLAYER);

        assertFalse(game.isGameOver());
        assertNull(game.getWinner());

        game.tick();

        assertFalse(game.isGameOver());
        assertFalse(game.isDraw());
        assertNull(game.getWinner());
    }

    @Test
    void computerAvoidsHeadOnDrawAgainstStraightMovingPlayer() {
        TronGame game = new TronGame(20, 10);

        for (int tick = 0; tick < 20 && !game.isGameOver(); tick++) {
            game.tick();
        }

        assertTrue(game.isGameOver());
        assertFalse(game.isDraw());
        assertEquals(TronGame.Player.COMPUTER, game.getWinner());
    }

    @Test
    void computerMovesTowardPlayerAcrossOpenBoard() {
        TronGame game = new TronGame(30, 20);

        game.tick();

        assertEquals(TronGame.Direction.LEFT, game.getComputerDirection());
    }
}
