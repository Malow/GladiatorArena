package com.github.malow.gladiatorarena.gamecore.unit;

import com.github.malow.gladiatorarena.gamecore.hex.Position;

public class Unit
{
  public int unitId;
  public String owner;
  public Position position;

  public double hitpoints = 10;

  public Unit(int unitId, String owner, Position position)
  {
    this.unitId = unitId;
    this.owner = owner;
    this.position = position;
  }

  public boolean isAlive()
  {
    return this.hitpoints > 0.0;
  }
}
