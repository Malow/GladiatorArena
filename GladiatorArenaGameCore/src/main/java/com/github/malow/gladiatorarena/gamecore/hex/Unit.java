package com.github.malow.gladiatorarena.gamecore.hex;

public class Unit
{
  private static int nextId = 0;

  private int unitId;
  public String owner;
  Position position;
  public double hitpoints = 10;

  public Unit(String owner, Position position)
  {
    this.unitId = nextId++;
    this.owner = owner;
    this.position = position;
  }

  public boolean isAlive()
  {
    return this.hitpoints > 0.0;
  }

  public Position getPosition()
  {
    return this.position;
  }

  public int getId()
  {
    return this.unitId;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.unitId;
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
    Unit other = (Unit) obj;
    if (this.unitId != other.unitId)
    {
      return false;
    }
    return true;
  }
}
