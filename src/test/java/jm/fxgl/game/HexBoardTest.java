package jm.fxgl.game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.geometry.Point2D;

class HexBoardTest {

    private HexBoard board;

    @BeforeEach
    void setUp() {
        long[][] types = {
                {7, 1, 7},
                {1, 17, 1},
                {1, 1, 1}
        };
        long[] walkable = {1, 17};
        board = new HexBoard(new Point2D(0, 20), 32, 28, types, walkable);
    }

    @Test
    void testFindTile() {
        Point2D point = new Point2D(16, 34);
        HexCoords tile = board.findTile(point);
        assertEquals(new HexCoords(0, 0), tile);
    }

    @Test
    void testTileCenter() {
        HexCoords tile = new HexCoords(0, 0);
        Point2D center = board.tileCenter(tile);
        assertEquals(new Point2D(16, 34), center);
    }

    @Test
    void testIsWalkable() {
        HexCoords walkableTile = new HexCoords(1, 0);
        HexCoords unWalkableTile = new HexCoords(0, 0);
        assertTrue(board.isWalkable(walkableTile));
        assertFalse(board.isWalkable(unWalkableTile));
    }
}
