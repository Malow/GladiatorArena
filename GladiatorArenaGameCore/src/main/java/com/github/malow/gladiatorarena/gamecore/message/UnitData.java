package com.github.malow.gladiatorarena.gamecore.message;

import com.github.malow.gladiatorarena.gamecore.hex.Position;
import com.github.malow.gladiatorarena.gamecore.hex.Unit;

public class UnitData
{
  public int unitId;
  public String owner;
  public Position position;
  public double hitpoints;

  public UnitData(Unit unit)
  {
    this.unitId = unit.getId();
    this.owner = unit.owner;
    this.position = unit.getPosition();
    this.hitpoints = unit.hitpoints;
  }
}
