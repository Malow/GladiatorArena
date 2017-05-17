package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class JoinGameMessage extends SocketMessage
{
  public String gameToken;

  public JoinGameMessage(String gameToken)
  {
    super(SocketMethod.JOIN_GAME);
    this.gameToken = gameToken;
  }

  @Override
  public boolean isValid()
  {
    if (super.isValid() && this.gameToken != null)
    {
      return true;
    }
    return false;
  }
}
