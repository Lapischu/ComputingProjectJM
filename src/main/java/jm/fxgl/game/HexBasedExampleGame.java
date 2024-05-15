package jm.fxgl.game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;

import java.util.List;

import com.almasb.fxgl.input.Input;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getInput;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.level.tiled.Layer;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;

import javafx.util.Duration;

public class HexBasedExampleGame extends GameApplication {

    private static final int HEIGHT = 288;
    private static final int WIDTH = 250;

    private static final double DELAY = 0.2;

    private HexBoard board;
    private AStarPathfinder pathfinder;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(WIDTH);
        settings.setHeight(HEIGHT);
        settings.setTitle("HexBasedExampleGame");
        settings.setManualResizeEnabled(true);
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new HexBasedGameFactory());
        FXGL.spawn("Background", new SpawnData(0, 0).put("width", WIDTH).put("height", HEIGHT));
        FXGL.setLevelFromMap("level1.tmx");

        GameWorld world = FXGL.getGameWorld();
        Entity entity = world.getSingleton(e -> e.isType("TiledMapLayer"));
        Layer layer = entity.getObject("layer");

        // get tile types, convert to 2D array
        List<Long> data = layer.getData();
        int width = layer.getWidth();
        int height = layer.getHeight();
        long[][] types = new long[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                types[col][row] = data.get(row * width + col);
            }
        }

        // 17 = ice, 1 = grass
        long[] walkable = {17, 1};

        // tile width and height (32, 28)
        // offset on Y access is 20 to comply with tile-map textures
        board = new HexBoard(new Point2D(0.0, 20.0), 32.0, 28.0, types, walkable);
        pathfinder = new AStarPathfinder(board);

        HexCoords startTile = new HexCoords(1, 1);
        setPlayerPosition(startTile);
    }


    @Override
    protected void initInput() {
        Input input = getInput();

        FXGL.onBtnDown(MouseButton.PRIMARY, () -> {
            // Get clicked position
            Point2D cursorPointInWorld = input.getMousePositionWorld();
            HexCoords targetTile = board.findTile(cursorPointInWorld);
            // System.out.println("Target tile "+targetTile+": type "+board.tileType(targetTile));

            if (! board.isWalkable(targetTile)) {
                System.out.println("Target tile "+targetTile+": type "+board.tileType(targetTile)+", not walkable!");
                return;
            }

            // Calculate path
            HexCoords startTile = getPlayerPosition();
            List<HexCoords> path = pathfinder.findPath(startTile, targetTile);
            // System.out.println(path);

            // Clear pending moves scheduled by runOnce()

            FXGL.getGameTimer().clear();

            // Start moving the player along the path with delays
            movePlayerAlongPath(path, 0);
        });
    }

    private void movePlayerAlongPath(List<HexCoords> path, int index) {
        if (index >= path.size()) {
            return; // Base case: all steps in the path have been completed
        }

        setPlayerPosition(path.get(index));

        // Add a delay to visualize the movement
        FXGL.runOnce(() -> {
            movePlayerAlongPath(path, index + 1); // Move to the next step in the path
        }, Duration.seconds(DELAY));
    }

    private static Entity getPlayer() {
        return FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
    }

    private void setPlayerPosition(HexCoords tile) {
        Entity player = getPlayer();
        Point2D playerCenter = player.getBoundingBoxComponent().getCenterLocal();
        Point2D tileCenter = board.tileCenter(tile);
        player.setPosition(tileCenter.subtract(playerCenter));
    }

    private HexCoords getPlayerPosition() {
        Entity player = getPlayer();
        Point2D playerCenter = player.getBoundingBoxComponent().getCenterLocal();
        Point2D tileCenter = player.getPosition().add(playerCenter);
        return board.findTile(tileCenter);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
