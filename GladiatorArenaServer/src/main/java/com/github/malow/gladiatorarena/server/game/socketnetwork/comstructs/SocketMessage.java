package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketMessage
{
  public String method;

  public SocketMessage(String method)
  {
    this.method = method;
  }

  public boolean isValid()
  {
    if (this.method != null) return true;
    return false;
  }
}
