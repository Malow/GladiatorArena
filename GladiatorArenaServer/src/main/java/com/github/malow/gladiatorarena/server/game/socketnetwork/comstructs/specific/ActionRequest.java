package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific;

import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;

public class ActionRequest extends SocketMessage
{
  public ActionRequest()
  {
    super(MethodNames.ACTION_REQUEST);
  }
}
