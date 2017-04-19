package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific;

import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;

public class GameStateUpdate extends SocketMessage
{
  public GameStateUpdate()
  {
    super(MethodNames.GAME_STATE_UPDATE);
  }
}
