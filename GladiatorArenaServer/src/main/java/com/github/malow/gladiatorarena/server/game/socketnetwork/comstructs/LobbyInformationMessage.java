package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

import java.util.Map;

public class LobbyInformationMessage extends SocketMessage
{
  public Map<String, Boolean> players;

  public LobbyInformationMessage(Map<String, Boolean> players)
  {
    super(SocketMethod.LOBBY_INFORMATION);
    this.players = players;
  }
}
