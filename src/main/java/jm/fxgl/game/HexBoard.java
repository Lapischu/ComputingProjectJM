package jm.fxgl.game;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class HexBoard {

    private final Point2D offset;
    private final double dx, dy;

    // tile type: types[col][row]
    private final long[][] types;

    // walkable tile types
    private final long[] walkable;

    public HexBoard(Point2D offset, double width, double height, long[][] types, long[] walkable) {
        this.offset = offset;
        this.dx = width / 4;
        this.dy = height / 2;
        this.types = types;
        this.walkable = walkable;
    }

    public HexCoords findTile(Point2D point) {
        point = point.subtract(offset);
        // System.out.println(point);

        // quotients
        // Divide the x coordinate by half the tile width
        double qx = point.getX() / dx;
        // Divide the y coordinate by half the tile height
        double qy = point.getY() / dy;

        // integral parts
        int ix = (int)Math.floor(qx);
        int iy = (int)Math.floor(qy);

        // fractional parts
        double fx = qx - ix;
        double fy = qy - iy;

        // make slightly negative values positive
        ix += 6;
        iy += 4;

        // modulo values
        int ixm6 = ix % 6;
        int iym2 = iy % 2;

        System.out.println("ix: "+ix+" fx: "+fx+" ixm6: "+ixm6);
        System.out.println("iy: "+iy+" fy: "+fy+" iym2: "+iym2);

        int col, row;
        if (
            ixm6 == 0 && iym2 == 0 && fx + fy > 1 || // Top left of Even hex / Bottom right of Odd hex - Even Case 1 / Odd Case 6
            ixm6 == 0 && iym2 == 1 && fx > fy || // Bottom left of Even hex / Top Right of Odd hex - Even Case 2 / Odd Case 5
            ixm6 == 1 ||  //  - Left center section of Even and Odd hexes - Even and Odd Case 3
            ixm6 == 2 || // Right center section of Even and Odd hexes - Even and Odd Case 4
            ixm6 == 3 && iym2 == 0 && fx < fy || // Top right of Even hex / Bottom left of Odd hex - Even Case 5 / Odd Case 2
            ixm6 == 3 && iym2 == 1 && fx + fy < 1 // Bottom right of Even hex / Top left of Odd hex - Even Case 6 / Odd Case 1
        ) {
            // even column
            col = ix / 6 * 2;
            // Divide integral y by two
            row = iy / 2;
        }
        else {
            // odd column
            col = (ix - 3) / 6 * 2 + 1;
            // Subtract 1 from integral y then divide by 2
            row = (iy - 1) / 2;
        }

        // restore slightly negative values
        col -= 2;
        row -= 2;

        return new HexCoords(col, row);
    }

    public Point2D tileCenter(HexCoords tile) {
        int col = tile.col();
        int row = tile.row();
        double x = (col * 3 + 2) * dx;
        double y = (row * 2 + 1) * dy;
        if (col % 2 == 1) y += dy;
        return new Point2D(x, y).add(offset);
    }

    public long tileType(HexCoords tile) {
        int col = tile.col();
        int row = tile.row();
        if (col < 0 || col >= types.length || row < 0 || row >= types[0].length) return -1;
        return types[col][row];
    }

    public boolean isWalkable(HexCoords tile) {
        long type = tileType(tile);
        for (long ok: walkable) if (type == ok) return true;
        return false;
    }

    public List<HexCoords> getWalkableNeighbors(HexCoords tile) {
        int odd = tile.col() % 2;
        List<HexCoords> neighbors = new ArrayList<>();
        addWalkableNeighbor(neighbors, tile, -1, odd - 1);
        addWalkableNeighbor(neighbors, tile, -1, odd);
        addWalkableNeighbor(neighbors, tile, 0, -1);
        addWalkableNeighbor(neighbors, tile, 0, 1);
        addWalkableNeighbor(neighbors, tile, 1, odd - 1);
        addWalkableNeighbor(neighbors, tile, 1, odd);
        return neighbors;
    }

    private void addWalkableNeighbor(List<HexCoords> neighbors, HexCoords tile, int dcol, int drow) {
        HexCoords neighbor = new HexCoords(tile.col() + dcol, tile.row() + drow);
        if (isWalkable(neighbor)) neighbors.add(neighbor);
    }
}
