package pl.mikolaj.games.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractBoard<T extends AbstractBoard.AbstractTile> {
    protected static final int TILE_SIZE = 100;
    protected static final int BOARD_SIZE = 3;
    protected static final String PLAYER_SYMBOL = "X";
    protected static final String COMPUTER_SYMBOL = "O";

    protected T[][] tiles;
    protected final List<Combo> combos = new ArrayList<>();

    protected void init(Class<T> tileClass, TileBuilder<T> tileBuilder) {
        @SuppressWarnings("unchecked")
        T[][] tempTiles = (T[][]) java.lang.reflect.Array.newInstance(tileClass, BOARD_SIZE, BOARD_SIZE);
        tiles = tempTiles;

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                T tile = tileBuilder.buildTile(y, x);
                tiles[y][x] = tile;
            }
        }

        //rows
        for (int y = 0; y < BOARD_SIZE; y++) {
            combos.add(new Combo(tiles[y]));
        }

        //columns
        for (int x = 0; x < BOARD_SIZE; x++) {
            List<T> column = new ArrayList<>(BOARD_SIZE);
            for (int y = 0; y < BOARD_SIZE; y++) {
                column.add(tiles[y][x]);
            }
            combos.add(new Combo(column));
        }

        //diagonals
        combos.add(createDiagonalCombo(true));
        combos.add(createDiagonalCombo(false));
    }

    private Combo createDiagonalCombo(boolean mainDiagonal) {
        List<T> diagonal = new ArrayList<>(BOARD_SIZE);
        for (int i = 0; i < BOARD_SIZE; i++) {
            diagonal.add(mainDiagonal ? tiles[i][i] : tiles[BOARD_SIZE - 1 - i][i]);
        }
        return new Combo(diagonal);
    }

    protected boolean isDraw() {
        return getEmptyTiles().isEmpty();
    }

    protected List<T> getEmptyTiles() {
        return Arrays.stream(tiles)
                .flatMap(Arrays::stream)
                .filter(T::isEmpty)
                .toList();
    }

    protected enum BoardState {
        NONE, X_WON, O_WON, DRAW
    }

    protected class Combo {
        protected final List<T> tiles;

        public Combo(List<T> tiles) {
            this.tiles = tiles;
        }

        public Combo(T[] tiles) {
            this.tiles = Arrays.asList(tiles);
        }

        public boolean isComplete() {
            if (tiles.getFirst().getValue().isEmpty()) {
                return false;
            }

            return tiles.stream()
                    .skip(1)
                    .allMatch(tile -> tiles.getFirst().getValue().equals(tile.getValue()));
        }

        public String getWinnerSymbol() {
            return tiles.getFirst().getValue();
        }
    }

    @FunctionalInterface
    protected interface TileBuilder<T extends AbstractTile> {
        T buildTile(int y, int x);
    }

    protected interface AbstractTile {
        String getValue();

        void setValue(String value);

        boolean isEmpty();
    }
}
