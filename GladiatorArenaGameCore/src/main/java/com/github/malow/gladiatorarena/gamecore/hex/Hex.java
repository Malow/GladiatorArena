package com.github.malow.gladiatorarena.gamecore.hex;

public class Hex extends Position
{
  public Double movementCost;

  public Hex(int x, int y)
  {
    super(x, y);
    this.movementCost = 1.0;
  }
}
