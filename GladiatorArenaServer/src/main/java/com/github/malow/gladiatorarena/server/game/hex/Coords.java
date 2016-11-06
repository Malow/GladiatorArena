package com.github.malow.gladiatorarena.server.game.hex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Coords
{

  protected int x;
  protected int y;

  public Coords(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public boolean isUpperX()
  {
    return this.x % 2 == 0;
  }

  public boolean isLowerX()
  {
    return this.x % 2 == 1;
  }

  public boolean isValid()
  {
    if (this.x >= 0 && this.x < HexagonMapSettings.sizeX && this.y >= 0 && this.y < HexagonMapSettings.sizeY) return true;
    return false;
  }

  public List<Coords> getCoordsForNeighbors()
  {
    List<Coords> neighbors = new ArrayList<Coords>();
    neighbors.add(new Coords(this.x, this.y + 1));
    neighbors.add(new Coords(this.x, this.y - 1));
    if (this.isLowerX())
    {
      neighbors.add(new Coords(this.x - 1, this.y));
      neighbors.add(new Coords(this.x - 1, this.y + 1));
      neighbors.add(new Coords(this.x + 1, this.y));
      neighbors.add(new Coords(this.x + 1, this.y + 1));
    }
    else
    {
      neighbors.add(new Coords(this.x - 1, this.y - 1));
      neighbors.add(new Coords(this.x - 1, this.y));
      neighbors.add(new Coords(this.x + 1, this.y - 1));
      neighbors.add(new Coords(this.x + 1, this.y));
    }
    return neighbors.stream().filter(n -> n.isValid()).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null) return false;
    if (!Hex.class.isAssignableFrom(obj.getClass())) { return false; }
    final Hex other = (Hex) obj;
    if (this.x == other.x && this.y == other.y) return true;
    return false;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 53 * hash + this.x;
    hash = 53 * hash + this.y;
    return hash;
  }

  @Override
  public String toString()
  {
    return this.x + "," + this.y;
  }
}
