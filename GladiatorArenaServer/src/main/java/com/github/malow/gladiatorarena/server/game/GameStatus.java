package com.github.malow.gladiatorarena.server.game;

public enum GameStatus
{
  NOT_STARTED(0), IN_PROGRESS(1), TIMED_OUT(2), FINISHED(3);
  public final Integer id;

  private GameStatus(Integer id)
  {
    this.id = id;
  }
}