package com.github.malow.gladiatorarena.server.game.hex;

public class Hex extends Coords
{

  private Double movementCost;

  public Hex(int x, int y)
  {
    super(x, y);
    this.movementCost = 1.0;
  }

  public Double getMovementCost()
  {
    return this.movementCost;
  }
}
