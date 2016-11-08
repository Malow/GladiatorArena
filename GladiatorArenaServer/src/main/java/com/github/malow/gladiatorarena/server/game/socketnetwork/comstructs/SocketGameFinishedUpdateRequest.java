package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketGameFinishedUpdateRequest extends SocketRequest
{
  public String winnerUsername;

  public SocketGameFinishedUpdateRequest(String method, String winnerUsername)
  {
    super(method);
    this.winnerUsername = winnerUsername;
  }

}
