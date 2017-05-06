package com.github.malow.gladiatorarena.gamecore.message;

public class GameFinishedUpdate extends Message
{
  public String winner;

  public GameFinishedUpdate(String winner)
  {
    this.winner = winner;
  }
}
