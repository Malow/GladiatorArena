package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketResponse extends SocketMessage
{
  public boolean result;

  public SocketResponse(SocketMethod method, boolean result)
  {
    super(method);
    this.result = result;
  }
}
