package com.github.malow.gladiatorarena.gamecore.message;

import com.github.malow.gladiatorarena.gamecore.hex.Position;

public class AttackAction extends Message
{
  public int unitId;
  public Position target;

  public AttackAction(int unitId, Position target)
  {
    this.unitId = unitId;
    this.target = target;
  }
}
