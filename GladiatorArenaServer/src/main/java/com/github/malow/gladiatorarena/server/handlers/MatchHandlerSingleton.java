package com.github.malow.gladiatorarena.server.handlers;

public enum MatchHandlerSingleton
{
  INSTANCE;

  private MatchHandler handler = new MatchHandler();

  public static MatchHandler get()
  {
    return INSTANCE.handler;
  }
}
