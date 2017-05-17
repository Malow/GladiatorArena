package com.github.malow.gladiatorarena.gamecore.message;

public class NextTurn extends Message
{
  public int currentUnitId;

  public NextTurn(int currentUnitId)
  {
    this.currentUnitId = currentUnitId;
  }
}
