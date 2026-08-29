package pl.mikolaj.games.tictactoe;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class VirtualBoard extends AbstractBoard<VirtualBoard.VirtualTile> {
    protected void init(AbstractTile[][] from) {
        super.init(VirtualTile.class, new TileBuilder(from));
    }

    public Pair<Integer, Integer> computerMove() {
        // Check if computer can win in the next move
        for (Combo combo : combos) {
            if (combo.canWin(COMPUTER_SYMBOL)) {
                VirtualTile tile = combo.getEmptyTile();
                return Pair.of(tile.getY(), tile.getX());
            }
        }

        // Check if player can win in the next move and block them
        for (Combo combo : combos) {
            if (combo.canWin(PLAYER_SYMBOL)) {
                VirtualTile tile = combo.getEmptyTile();
                return Pair.of(tile.getY(), tile.getX());
            }
        }

        // Otherwise, make a random move
        return findRandomMove();
    }

    private boolean checkWinner() {
        return combos.stream()
                .anyMatch(Combo::isComplete);
    }

    private Pair<Integer, Integer> findRandomMove() {
        List<VirtualTile> emptyTiles = getEmptyTiles();
        Random random = new Random();
        int next = random.nextInt(emptyTiles.size());
        VirtualTile tile = emptyTiles.get(next);
        return Pair.of(tile.getY(), tile.getX());
    }

    private static class TileBuilder implements AbstractBoard.TileBuilder<VirtualTile> {
        AbstractTile[][] from;
        private TileBuilder(AbstractTile[][] from) {
            this.from = from;
        }

        public VirtualTile buildTile(int y, int x) {
                VirtualTile newTile = new VirtualTile();
                newTile.setValue(from[y][x].getValue());
                newTile.setY(y);
                newTile.setX(x);
                return newTile;
        }
    }

    protected static class VirtualTile implements AbstractTile {

        private String value = StringUtils.EMPTY;
        private int y,x;

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        public void clearValue() {
            value = StringUtils.EMPTY;
        }

        @Override
        public boolean isEmpty() {
            return StringUtils.isEmpty(value);
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }
    }
}
