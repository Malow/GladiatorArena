package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class SocketJoinGameRequest extends SocketRequest
{
  public String email;
  public String authToken;
  public Long gameId;

  public SocketJoinGameRequest(String method, String email, String authToken, Long gameId)
  {
    super(method);
    this.email = email;
    this.authToken = authToken;
    this.gameId = gameId;
  }

  @Override
  public boolean isValid()
  {
    if (super.isValid() && this.email != null && this.authToken != null && this.gameId != null) return true;
    return false;
  }
}
