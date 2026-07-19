package pl.mikolaj.games.tictactoe;

import org.apache.commons.lang3.StringUtils;

public class VirtualBoard extends AbstractBoard<VirtualBoard.VirtualTile> {
    protected void init(AbstractTile[][] from) {
        super.init(VirtualTile.class, new TileBuilder(from));
    }

    private static class TileBuilder implements AbstractBoard.TileBuilder<VirtualTile> {
        AbstractTile[][] from;
        private TileBuilder(AbstractTile[][] from) {
            this.from = from;
        }

        public VirtualTile buildTile(int y, int x) {
                VirtualTile newTile = new VirtualTile();
                newTile.setValue(from[y][x].getValue());
                return newTile;
        }
    }

    protected static class VirtualTile implements AbstractTile {

        private String value = "";

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean isEmpty() {
            return StringUtils.isEmpty(value);
        }
    }
}
