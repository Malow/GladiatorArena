package com.github.malow.gladiatorarena.server;

public class GladiatorArenaServerConfig
{
  public boolean allowTestOperations = false;
  public int gameSocketServerPort;

  public GladiatorArenaServerConfig(int gameSocketServerPort)
  {
    this.gameSocketServerPort = gameSocketServerPort;
  }

  // Static game settings, move in the future
  public static final int DEFAULT_RATING = 0;
  public static final int MATCHMAKING_MAX_RATING_DIFFERENCE = 100;

  public static final int PRE_GAME_TIMEOUT_SECONDS = 60;
  public static final int RECONNECT_TIMEOUT_SECONDS = 60;
  public static final int POST_GAME_DURATION_SECONDS = 30;
  public static final int SLEEP_DURATION_IN_LOBBY = 100;
}
