package com.github.malow.gladiatorarena.gamecore.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HexagonMap
{
  private Hexagon[][] map;
  private int sizeX;
  private int sizeY;

  public HexagonMap(int sizeX, int sizeY)
  {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.map = new Hexagon[sizeX][sizeY];
    for (int i = 0; i < sizeX; i++)
    {
      for (int u = 0; u < sizeY; u++)
      {
        this.map[i][u] = new Hexagon(i, u);
      }
    }
  }

  public Hexagon get(Position position)
  {
    return this.map[position.x][position.y];
  }

  public List<Hexagon> getAStarPath(Hexagon start, Hexagon goal)
  {
    List<Hexagon> closedSet = new ArrayList<Hexagon>();
    List<Hexagon> openSet = new ArrayList<Hexagon>();
    openSet.add(start);
    Map<Hexagon, Hexagon> cameFrom = new HashMap<Hexagon, Hexagon>();
    Map<Hexagon, Double> gScore = new HashMap<Hexagon, Double>();
    gScore.put(start, 0.0);
    Map<Hexagon, Double> fScore = new HashMap<Hexagon, Double>();
    fScore.put(start, Double.valueOf(HexagonHelper.getDistanceInHexes(start, goal)));
    while (!openSet.isEmpty())
    {
      Hexagon current = fScore.entrySet().stream().filter(a -> openSet.contains(a.getKey())).min((a, b) -> Double.compare(a.getValue(), b.getValue()))
          .get().getKey();
      if (current.equals(goal))
      {
        return this.reconstruct_path(cameFrom, current);
      }
      openSet.remove(current);
      closedSet.add(current);
      List<Hexagon> neighbors = this.getNeighborsForHex(current);
      for (Hexagon neighbor : neighbors)
      {
        if (closedSet.contains(neighbor))
        {
          continue;
        }
        double tentative_gScore = gScore.get(current) + neighbor.movementCost;
        if (!openSet.contains(neighbor))
        {
          openSet.add(neighbor);
        }
        else if (tentative_gScore >= gScore.get(neighbor))
        {
          continue;
        }
        cameFrom.put(neighbor, current);
        gScore.put(neighbor, tentative_gScore);
        fScore.put(neighbor, gScore.get(neighbor) + HexagonHelper.getDistanceInHexes(start, goal));
      }
    }
    return null;
  }

  private List<Hexagon> reconstruct_path(Map<Hexagon, Hexagon> cameFrom, Hexagon current)
  {
    List<Hexagon> totalPath = new ArrayList<Hexagon>();
    boolean run = cameFrom.containsKey(current);
    while (run)
    {
      current = cameFrom.get(current);
      if (cameFrom.containsKey(current))
      {
        totalPath.add(current);
      }
      else
      {
        run = false;
      }
    }
    Collections.reverse(totalPath);
    return totalPath;
  }

  public List<Hexagon> getNeighborsForHex(Hexagon hex)
  {
    List<Hexagon> neighbors = new ArrayList<Hexagon>();
    List<Position> coords = HexagonHelper.getCoordsForNeighbors(hex, this.sizeX, this.sizeY);
    for (Position coord : coords)
    {
      neighbors.add(this.map[coord.x][coord.y]);
    }
    return neighbors;
  }
}
