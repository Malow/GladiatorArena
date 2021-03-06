package com.github.malow.gladiatorarena.gamecore.hex;

public class Position
{
  public int x;
  public int y;

  public Position(int x, int y)
  {
    this.x = x;
    this.y = y;
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
    Position other = (Position) obj;
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
