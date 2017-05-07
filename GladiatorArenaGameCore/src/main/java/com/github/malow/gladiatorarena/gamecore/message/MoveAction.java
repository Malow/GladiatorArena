package com.github.malow.gladiatorarena.gamecore.message;

import java.util.List;

import com.github.malow.gladiatorarena.gamecore.hex.Position;

public class MoveAction extends Message
{
  public int unitId;
  public List<Position> path;

  public MoveAction(int unitId, List<Position> path)
  {
    this.unitId = unitId;
    this.path = path;
  }
}
