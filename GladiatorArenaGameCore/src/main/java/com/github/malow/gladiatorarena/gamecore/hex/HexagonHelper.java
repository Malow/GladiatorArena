package com.github.malow.gladiatorarena.gamecore.hex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HexagonHelper
{
  private static boolean isLower(int x)
  {
    return x % 2 == 1;
  }

  public static List<Position> getCoordsForNeighbors(Position coord, int sizeX, int sizeY)
  {
    List<Position> neighbors = new ArrayList<Position>();
    neighbors.add(new Position(coord.x, coord.y + 1));
    neighbors.add(new Position(coord.x, coord.y - 1));
    if (isLower(coord.x))
    {
      neighbors.add(new Position(coord.x - 1, coord.y));
      neighbors.add(new Position(coord.x - 1, coord.y + 1));
      neighbors.add(new Position(coord.x + 1, coord.y));
      neighbors.add(new Position(coord.x + 1, coord.y + 1));
    }
    else
    {
      neighbors.add(new Position(coord.x - 1, coord.y - 1));
      neighbors.add(new Position(coord.x - 1, coord.y));
      neighbors.add(new Position(coord.x + 1, coord.y - 1));
      neighbors.add(new Position(coord.x + 1, coord.y));
    }
    return neighbors.stream().filter(n ->
    {
      return n.x >= 0 && n.x < sizeX && n.y >= 0 && n.y < sizeY;
    }).collect(Collectors.toList());
  }

  public static String visualizeHexagonMapWithDistances(int sizeX, int sizeY, Hexagon from)
  {
    String s = "";
    for (int u = 0; u < sizeY; u++)
    {
      for (int i = 0; i < sizeX; i++)
      {
        if (i % 2 == 0)
        {
          int distance = HexagonHelper.getDistanceInHexes(from, new Hexagon(i, u));
          s += (distance + "").length() == 2 ? distance + " " : distance + "  ";
        }
        else
        {
          s += "   ";
        }
      }
      s += "\n";
      for (int i = 0; i < sizeX; i++)
      {
        if (i % 2 == 1)
        {
          int distance = HexagonHelper.getDistanceInHexes(from, new Hexagon(i, u));
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

  public static String visualizeHexagonMapWithPositions(int sizeX, int sizeY)
  {
    String s = "";
    for (int u = 0; u < sizeY; u++)
    {
      for (int i = 0; i < sizeX; i++)
      {
        if (i % 2 == 0)
        {
          String position = new Hexagon(i, u).toString();
          s += position + " ";
        }
        else
        {
          s += "   ";
        }
      }
      s += "\n";
      for (int i = 0; i < sizeX; i++)
      {
        if (i % 2 == 1)
        {
          String position = new Hexagon(i, u).toString();
          s += position + " ";
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

  public static int getDistanceInHexes(Hexagon from, Hexagon to)
  {
    Hexagon moveCoords = new Hexagon(from.x, from.y);
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

  public static boolean isAdjacent(Position one, Position two)
  {
    if (one.x == two.x && (one.y == two.y + 1 || one.y == two.y - 1))
    {
      return true;
    }
    if (isLower(two.x))
    {
      if (one.x == two.x - 1 && one.y == two.y || one.x == two.x - 1 && one.y == two.y + 1 || one.x == two.x + 1 && one.y == two.y
          || one.x == two.x + 1 && one.y == two.y + 1)
      {
        return true;
      }
    }
    else
    {
      if (one.x == two.x - 1 && one.y == two.y - 1 || one.x == two.x - 1 && one.y == two.y || one.x == two.x + 1 && one.y == two.y - 1
          || one.x == two.x + 1 && one.y == two.y)
      {
        return true;
      }
    }
    return false;
  }

  private static int getMoveStep(int current, int target)
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
}
