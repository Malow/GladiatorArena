package com.github.malow.gladiatorarena.server.game.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HexagonMap
{

  private Hex[][] map;

  public HexagonMap()
  {
    this.map = new Hex[HexagonMapSettings.sizeX][HexagonMapSettings.sizeY];
    for (int i = 0; i < HexagonMapSettings.sizeX; i++)
    {
      for (int u = 0; u < HexagonMapSettings.sizeY; u++)
      {
        this.map[i][u] = new Hex(i, u);
      }
    }
  }

  public int getDistanceInHexes(Hex from, Hex to)
  {
    Hex moveCoords = new Hex(from.x, from.y);
    int steps = 0;
    while (moveCoords.x != to.x || moveCoords.y != to.y)
    {
      steps++;
      if (moveCoords.x == to.x) // Easy move Y
      {
        moveCoords.y += getMoveStep(moveCoords.y, to.y);
      }
      else
      {
        if (moveCoords.y != to.y) // Both Y and X are wrong, Diagonal move
        {
          if (moveCoords.y < to.y)
          {
            if (moveCoords.isLowerX())
            {
              moveCoords.y++;
            }
          }
          else
          {
            if (moveCoords.isUpperX())
            {
              moveCoords.y--;
            }
          }
        }
        moveCoords.x += getMoveStep(moveCoords.x, to.x);
      }
    }
    return steps;
  }

  private int getMoveStep(int current, int target)
  {
    if (current < target)
    {
      return 1;
    }
    else if (current > target) { return -1; }
    return 0;
  }

  public String getAsGraphicalStringWithDistances(Hex from)
  {
    String s = "";
    for (int u = 0; u < HexagonMapSettings.sizeY; u++)
    {
      for (int i = 0; i < HexagonMapSettings.sizeX; i++)
      {
        if (i % 2 == 0)
        {
          int distance = this.getDistanceInHexes(from, new Hex(i, u));
          s += (distance + "").length() == 2 ? distance + " " : distance + "  ";
        }
        else
        {
          s += "   ";
        }
      }
      s += "\n";
      for (int i = 0; i < HexagonMapSettings.sizeX; i++)
      {
        if (i % 2 == 1)
        {
          int distance = this.getDistanceInHexes(from, new Hex(i, u));
          s += (distance + "").length() == 2 ? distance + " " : distance + "  ";
        }
        else
        {
          s += "   ";
        }
      }
      s += "\n";
    }
    return s;
  }

  public List<Hex> aStar(Hex start, Hex goal)
  {
    List<Hex> closedSet = new ArrayList<Hex>();
    List<Hex> openSet = new ArrayList<Hex>();
    openSet.add(start);
    Map<Hex, Hex> cameFrom = new HashMap<Hex, Hex>();
    Map<Hex, Double> gScore = new HashMap<Hex, Double>();
    gScore.put(start, 0.0);
    Map<Hex, Double> fScore = new HashMap<Hex, Double>();
    fScore.put(start, new Double(getDistanceInHexes(start, goal)));
    while (!openSet.isEmpty())
    {
      Hex current = fScore.entrySet().stream().filter(a -> openSet.contains(a.getKey())).min((a, b) -> Double.compare(a.getValue(), b.getValue()))
          .get().getKey();
      if (current.equals(goal)) { return this.reconstruct_path(cameFrom, current); }
      openSet.remove(current);
      closedSet.add(current);
      List<Hex> neighbors = this.getNeighborsForHex(current);
      for (Hex neighbor : neighbors)
      {
        if (closedSet.contains(neighbor))
        {
          continue;
        }
        double tentative_gScore = gScore.get(current) + neighbor.getMovementCost();
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
        fScore.put(neighbor, gScore.get(neighbor) + getDistanceInHexes(start, goal));
      }
    }
    return null;
  }

  private List<Hex> reconstruct_path(Map<Hex, Hex> cameFrom, Hex current)
  {
    List<Hex> totalPath = new ArrayList<Hex>();
    while (cameFrom.containsKey(current))
    {
      current = cameFrom.get(current);
      totalPath.add(current);
    }
    return totalPath;
  }

  private List<Hex> getNeighborsForHex(Hex hex)
  {
    List<Hex> neighbors = new ArrayList<Hex>();
    List<Coords> coords = hex.getCoordsForNeighbors();
    for (Coords coord : coords)
    {
      neighbors.add(this.map[coord.x][coord.y]);
    }
    return neighbors;
  }
}
