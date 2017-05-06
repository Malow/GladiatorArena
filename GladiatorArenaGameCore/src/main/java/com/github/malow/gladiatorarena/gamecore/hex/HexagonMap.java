package com.github.malow.gladiatorarena.gamecore.hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HexagonMap
{
  private Hex[][] map;
  private int sizeX;
  private int sizeY;

  public HexagonMap(int sizeX, int sizeY)
  {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.map = new Hex[sizeX][sizeY];
    for (int i = 0; i < sizeX; i++)
    {
      for (int u = 0; u < sizeY; u++)
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
        moveCoords.y += this.getMoveStep(moveCoords.y, to.y);
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
        moveCoords.x += this.getMoveStep(moveCoords.x, to.x);
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
    else if (current > target)
    {
      return -1;
    }
    return 0;
  }

  public String getAsGraphicalStringWithDistances(Hex from)
  {
    String s = "";
    for (int u = 0; u < this.sizeY; u++)
    {
      for (int i = 0; i < this.sizeX; i++)
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
      for (int i = 0; i < this.sizeX; i++)
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
    fScore.put(start, Double.valueOf(this.getDistanceInHexes(start, goal)));
    while (!openSet.isEmpty())
    {
      Hex current = fScore.entrySet().stream().filter(a -> openSet.contains(a.getKey())).min((a, b) -> Double.compare(a.getValue(), b.getValue()))
          .get().getKey();
      if (current.equals(goal))
      {
        return this.reconstruct_path(cameFrom, current);
      }
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
        fScore.put(neighbor, gScore.get(neighbor) + this.getDistanceInHexes(start, goal));
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

  public List<Hex> getNeighborsForHex(Hex hex)
  {
    List<Hex> neighbors = new ArrayList<Hex>();
    List<Coords> coords = this.getCoordsForNeighbors(hex);
    for (Coords coord : coords)
    {
      neighbors.add(this.map[coord.x][coord.y]);
    }
    return neighbors;
  }

  private List<Coords> getCoordsForNeighbors(Coords coord)
  {
    List<Coords> neighbors = new ArrayList<Coords>();
    neighbors.add(new Coords(coord.x, coord.y + 1));
    neighbors.add(new Coords(coord.x, coord.y - 1));
    if (coord.isLowerX())
    {
      neighbors.add(new Coords(coord.x - 1, coord.y));
      neighbors.add(new Coords(coord.x - 1, coord.y + 1));
      neighbors.add(new Coords(coord.x + 1, coord.y));
      neighbors.add(new Coords(coord.x + 1, coord.y + 1));
    }
    else
    {
      neighbors.add(new Coords(coord.x - 1, coord.y - 1));
      neighbors.add(new Coords(coord.x - 1, coord.y));
      neighbors.add(new Coords(coord.x + 1, coord.y - 1));
      neighbors.add(new Coords(coord.x + 1, coord.y));
    }
    return neighbors.stream().filter(n ->
    {
      return n.x >= 0 && n.x < this.sizeX && n.y >= 0 && n.y < this.sizeY;
    }).collect(Collectors.toList());
  }
}
