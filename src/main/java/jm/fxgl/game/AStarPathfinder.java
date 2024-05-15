package jm.fxgl.game;

import java.util.*;

public class AStarPathfinder {

    private final HexBoard board;

    public AStarPathfinder(HexBoard board) {
        this.board = board;
    }

    public List<HexCoords> findPath(HexCoords start, HexCoords end) {
        Set<HexCoords> visited = new HashSet<>();
        Map<HexCoords, HexCoords> cameFrom = new HashMap<>();
        Map<HexCoords, Double> gScore = new HashMap<>();
        Map<HexCoords, Double> fScore = new HashMap<>();

        PriorityQueue<HexCoords> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));

        gScore.put(start, 0.0);
        fScore.put(start, heuristicCostEstimate(start, end));
        openSet.add(start);

        while (!openSet.isEmpty()) {
            HexCoords current = openSet.poll();

            if (current.equals(end)) {
                return reconstructPath(cameFrom, current);
            }

            visited.add(current);

            List<HexCoords> neighbors = board.getWalkableNeighbors(current);
            // System.out.println("Current: "+current+", walkable neighbors: "+neighbors);
            for (HexCoords neighbor: neighbors) {
                if (visited.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + 1.0; // Assuming each step cost is 1

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristicCostEstimate(neighbor, end));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    private List<HexCoords> reconstructPath(Map<HexCoords, HexCoords> cameFrom, HexCoords current) {
        List<HexCoords> path = new ArrayList<>();
        path.add(current);

        // Follow the parent nodes from the goal node back to the start node
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }

        // Reverse the path to get it from start to end
        Collections.reverse(path);
        return path;
    }

    private double heuristicCostEstimate(HexCoords start, HexCoords end) {
        int dcol = Math.abs(start.col() - end.col());
        int drow = Math.abs(start.row() - end.row());
        return Math.max(dcol, drow);
    }
}
