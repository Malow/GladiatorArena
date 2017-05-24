package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

import java.util.Map;

public class LobbyInformationMessage extends SocketMessage
{
  public String yourUsername;
  public Map<String, Boolean> players;

  public LobbyInformationMessage(Map<String, Boolean> players, String yourUsername)
  {
    super(SocketMethod.LOBBY_INFORMATION);
    this.players = players;
    this.yourUsername = yourUsername;
  }
}
