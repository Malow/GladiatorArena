package com.github.malow.gladiatorarena.server.game;

public enum GameStatus
{
  NOT_STARTED(0), TIMED_OUT(1), FINISHED(2);
  public final Integer id;

  private GameStatus(Integer id)
  {
    this.id = id;
  }
}