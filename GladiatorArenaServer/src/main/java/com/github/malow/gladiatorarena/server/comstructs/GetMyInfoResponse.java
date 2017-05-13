package com.github.malow.gladiatorarena.server.comstructs;

import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.User;

public class GetMyInfoResponse extends Response
{
  public String username;
  public Double rating;
  public String currentGameToken;
  public boolean isSearchingForGame;

  public GetMyInfoResponse(boolean result, User user)
  {
    super(result);
    this.username = user.username;
    this.rating = user.rating;
    this.currentGameToken = user.currentGameToken;
    this.isSearchingForGame = user.isSearchingForGame;
  }

  public GetMyInfoResponse(boolean result, String username, Double rating, String currentGameToken, boolean isSearchingForGame)
  {
    super(result);
    this.username = username;
    this.rating = rating;
    this.currentGameToken = currentGameToken;
    this.isSearchingForGame = isSearchingForGame;
  }

}
