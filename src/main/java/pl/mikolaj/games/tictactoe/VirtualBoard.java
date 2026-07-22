package pl.mikolaj.games.tictactoe;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VirtualBoard extends AbstractBoard<VirtualBoard.VirtualTile> {
    protected void init(AbstractTile[][] from) {
        super.init(VirtualTile.class, new TileBuilder(from));
    }

    public Pair<Integer, Integer> findBestMove() {
        for (VirtualTile emptyTile : getEmptyTiles()) {
            emptyTile.setValue(COMPUTER_SYMBOL);
            BoardState boardState = checkState();
            switch (boardState) {
                case O_WON, DRAW:
                    return Pair.of(emptyTile.getY(), emptyTile.getX());
                case NONE:
                    emptyTile.clearValue();
            }
        }
        return findRandomMove();
    }

    private BoardState checkState() {
        Optional<Combo> winner = combos.stream()
                .filter(Combo::isComplete)
                .findFirst();

        if (winner.isPresent()) {
            return BoardState.O_WON;
        } else if (isDraw()){
            return BoardState.DRAW;
        } else {
            return BoardState.NONE;
        }
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
