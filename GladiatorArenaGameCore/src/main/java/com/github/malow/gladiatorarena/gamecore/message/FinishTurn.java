package com.github.malow.gladiatorarena.gamecore.message;

public class FinishTurn extends Message
{
  public int unitId;

  public FinishTurn(int unitId)
  {
    this.unitId = unitId;
  }
}
