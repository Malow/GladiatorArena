package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

import com.github.malow.gladiatorarena.server.game.GameStatus;

public class GameStatusUpdate extends SocketMessage
{
  public GameStatus newStatus;

  public GameStatusUpdate(GameStatus newStatus)
  {
    super(SocketMethod.GAME_STATUS_UPDATE);
    this.newStatus = newStatus;
  }
}
