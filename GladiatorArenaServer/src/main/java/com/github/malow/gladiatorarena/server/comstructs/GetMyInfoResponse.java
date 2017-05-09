package com.github.malow.gladiatorarena.server.comstructs;

import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.User;

public class GetMyInfoResponse extends Response
{
  public String username;
  public Double rating;
  public Integer currentGameId;
  public boolean isSearchingForGame;

  public GetMyInfoResponse(boolean result, User user)
  {
    super(result);
    this.username = user.username;
    this.rating = user.rating;
    this.currentGameId = user.currentGameId;
    this.isSearchingForGame = user.isSearchingForGame;
  }

  public GetMyInfoResponse(boolean result, String username, Double rating, Integer currentGameId, boolean isSearchingForGame)
  {
    super(result);
    this.username = username;
    this.rating = rating;
    this.currentGameId = currentGameId;
    this.isSearchingForGame = isSearchingForGame;
  }

}
