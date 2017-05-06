package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketErrorResponse extends SocketResponse
{
  public String error;

  public SocketErrorResponse(SocketMethod method, boolean result, String error)
  {
    super(method, result);
    this.error = error;
  }
}
