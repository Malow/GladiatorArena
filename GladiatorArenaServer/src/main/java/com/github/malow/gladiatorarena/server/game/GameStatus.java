package com.github.malow.gladiatorarena.server.game;

public enum GameStatus
{
  NOT_STARTED,
  IN_PROGRESS,
  PAUSED_FOR_RECONNECT,
  TIMED_OUT,
  FINISHED;
}