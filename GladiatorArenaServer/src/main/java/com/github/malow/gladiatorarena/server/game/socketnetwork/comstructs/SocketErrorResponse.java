package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketErrorResponse extends SocketMessage
{
  public SocketMethod requestMethod;
  public String error;

  public SocketErrorResponse(SocketMethod requestMethod, String error)
  {
    super(SocketMethod.ERROR_RESPONSE);
    this.requestMethod = requestMethod;
    this.error = error;
  }
}
