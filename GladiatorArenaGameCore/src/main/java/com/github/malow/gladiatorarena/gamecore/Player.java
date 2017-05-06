package com.github.malow.gladiatorarena.gamecore;

public abstract class Player
{
  public String username;
  public boolean hasFinishedTurn = false;

  public Player(String username)
  {
    this.username = username;
  }

  public abstract void gameStateUpdate();

  public abstract void gameFinishedUpdate(String winner);
}
