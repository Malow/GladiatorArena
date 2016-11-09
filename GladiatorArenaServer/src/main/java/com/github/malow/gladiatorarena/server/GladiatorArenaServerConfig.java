package com.github.malow.gladiatorarena.server;

public class GladiatorArenaServerConfig
{
  public boolean allowClearCacheOperation = false;

  // Static game settings, move in the future
  public static final int DEFAULT_RATING = 0;
  public static final int MATCHMAKING_MAX_RATING_DIFFERENCE = 100;
  public static final int PRE_GAME_TIMEOUT_SECONDS = 60;
  public static final int GAME_ROUND_TIMEOUT_SECONDS = 30;
  public static final int POST_GAME_DURATION_SECONDS = 30;
  public static final int SLEEP_DURATION_BETWEEN_LOBBY_UPDATES_MILLISECONDS = 100;
  //
  public static final String JOIN_GAME_REQUEST_NAME = "JoinGame";
  public static final String READY_REQUEST_NAME = "Ready";
  public static final String GAME_STATE_UPDATE_REQUEST_NAME = "GameUpdate";
  public static final String GAME_FINISHED_UPDATE_REQUEST_NAME = "GameFinsished";

  public GladiatorArenaServerConfig()
  {

  }
}
