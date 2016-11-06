package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketResponse
{
  public boolean result;
  public String method;

  public SocketResponse(String method, boolean result)
  {
    this.result = result;
    this.method = method;
  }
}
