package com.github.malow.gladiatorarena.gamecore.hex;

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

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.x;
    result = prime * result + this.y;
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (this.getClass() != obj.getClass())
    {
      return false;
    }
    Coords other = (Coords) obj;
    if (this.x != other.x)
    {
      return false;
    }
    if (this.y != other.y)
    {
      return false;
    }
    return true;
  }

  @Override
  public String toString()
  {
    return this.x + "," + this.y;
  }
}
