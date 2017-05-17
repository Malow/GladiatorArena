package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketMessage
{
  public static enum SocketMethod
  {
    UNKNOWN,
    JOIN_GAME,
    READY,
    ERROR_RESPONSE,
    LOBBY_INFORMATION,
    GAME_STATUS_UPDATE,
    GAME_MESSAGE
  }

  public SocketMethod method;

  public SocketMessage(SocketMethod method)
  {
    this.method = method;
  }

  public boolean isValid()
  {
    if (this.method != null)
    {
      return true;
    }
    return false;
  }
}
