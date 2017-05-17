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
    // Needed because the getPosition is a hex object, which has a reference to the unit, which causes stack overflow when gson
    this.position = new Position(unit.getPosition().x, unit.getPosition().y);
    this.hitpoints = unit.hitpoints;
  }
}
