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

    // Creating a new Position instead of just using unit's position is needed because getPosition returns a Hexagon object which is an extension of the Position class.
    // The Hexagon itself has a reference to the unit on it which causes a stack overflow when gson tries to serialize this object.
    // As such we need to create a simple Position-copy of the Hexagon only retaining its coordinates.
    this.position = new Position(unit.getPosition().x, unit.getPosition().y);

    this.hitpoints = unit.hitpoints;
  }
}
