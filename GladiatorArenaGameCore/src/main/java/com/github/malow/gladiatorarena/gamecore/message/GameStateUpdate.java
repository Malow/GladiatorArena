package com.github.malow.gladiatorarena.gamecore.message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.malow.gladiatorarena.gamecore.hex.Unit;

public class GameStateUpdate extends Message
{
  public List<UnitData> units = new ArrayList<>();

  public GameStateUpdate(List<Unit> units)
  {
    this.units = units.stream().map(u -> new UnitData(u)).collect(Collectors.toList());
  }
}
