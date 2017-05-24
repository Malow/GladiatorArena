package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class PlayerConnectedMessage extends SocketMessage
{
  public String username;

  public PlayerConnectedMessage(String username)
  {
    super(SocketMethod.PLAYER_CONNECTED);
    this.username = username;
  }

}
