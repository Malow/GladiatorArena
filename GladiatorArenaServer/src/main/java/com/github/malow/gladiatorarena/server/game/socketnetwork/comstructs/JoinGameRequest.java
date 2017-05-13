package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class JoinGameRequest extends SocketMessage
{
  public String gameToken;

  public JoinGameRequest(String gameToken)
  {
    super(SocketMethod.JOIN_GAME_REQUEST);
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
