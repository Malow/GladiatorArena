package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.matchmakingengine.MatchmakingEngine;
import com.github.malow.malowlib.matchmakingengine.MatchmakingEngineConfig;

public enum MatchmakingEngineSingleton
{
  INSTANCE;

  private MatchmakingEngine engine;

  public static MatchmakingEngine get()
  {
    return INSTANCE.engine;
  }

  public static void init(MatchmakingEngineConfig config, MaloWProcess listener)
  {
    INSTANCE.engine = new MatchmakingEngine(config, listener);
  }
}
