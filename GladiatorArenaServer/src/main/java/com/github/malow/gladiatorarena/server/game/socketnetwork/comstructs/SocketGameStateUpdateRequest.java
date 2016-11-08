package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketGameStateUpdateRequest extends SocketRequest
{
  public String test;

  public SocketGameStateUpdateRequest(String method, String test)
  {
    super(method);
    this.test = test;
  }
}
