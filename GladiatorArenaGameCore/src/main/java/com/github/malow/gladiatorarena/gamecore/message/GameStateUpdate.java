package com.github.malow.gladiatorarena.gamecore.message;

import java.util.List;

import com.github.malow.gladiatorarena.gamecore.unit.Unit;

public class GameStateUpdate extends Message
{
  public List<Unit> units;

  public GameStateUpdate(List<Unit> units)
  {
    this.units = units;
  }
}
