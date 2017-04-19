package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific;

import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;

public class GameFinishedUpdate extends SocketMessage
{
  public String winnerUsername;

  public GameFinishedUpdate(String winnerUsername)
  {
    super(MethodNames.GAME_FINISHED_UPDATE);
    this.winnerUsername = winnerUsername;
  }

}
