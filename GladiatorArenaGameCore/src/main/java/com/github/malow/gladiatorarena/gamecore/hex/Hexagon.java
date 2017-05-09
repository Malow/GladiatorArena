package com.github.malow.gladiatorarena.gamecore.hex;

public class Hexagon extends Position
{
  public Double movementCost;
  private Unit unit;

  public Hexagon(int x, int y)
  {
    super(x, y);
    this.movementCost = 1.0;
  }

  public boolean isUpperX()
  {
    return this.x % 2 == 0;
  }

  public boolean isLowerX()
  {
    return this.x % 2 == 1;
  }

  public boolean isOccupied()
  {
    return this.unit != null;
  }

  public void setUnit(Unit unit)
  {
    if (this.isOccupied())
    {
      throw new RuntimeException("Space is already occupied.");
    }
    unit.position = this;
    this.unit = unit;
  }

  public void clearTile()
  {
    this.unit = null;
  }

  public Unit getUnit()
  {
    return this.unit;
  }
}
