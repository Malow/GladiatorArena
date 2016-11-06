package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketRequest
{
  public String method;

  public SocketRequest(String method)
  {
    this.method = method;
  }

  public boolean isValid()
  {
    if (this.method != null) return true;
    return false;
  }
}
