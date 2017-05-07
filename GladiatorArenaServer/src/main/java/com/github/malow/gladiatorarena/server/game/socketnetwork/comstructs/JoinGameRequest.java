package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class JoinGameRequest extends SocketMessage
{
  public String email;
  public String authToken;
  public Integer gameId;

  public JoinGameRequest(String email, String authToken, Integer gameId)
  {
    super(SocketMethod.JOIN_GAME_REQUEST);
    this.email = email;
    this.authToken = authToken;
    this.gameId = gameId;
  }

  @Override
  public boolean isValid()
  {
    if (super.isValid() && this.email != null && this.authToken != null && this.gameId != null)
    {
      return true;
    }
    return false;
  }
}