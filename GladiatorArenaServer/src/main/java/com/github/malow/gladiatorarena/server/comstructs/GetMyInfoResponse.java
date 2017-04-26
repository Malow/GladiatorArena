package com.github.malow.gladiatorarena.server.comstructs;

import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.Player;

public class GetMyInfoResponse extends Response
{
  public String username;
  public Integer rating;
  public Integer currentGameId;
  public boolean isSearchingForGame;

  public GetMyInfoResponse(boolean result, Player player)
  {
    super(result);
    this.username = player.username;
    this.rating = player.rating;
    this.currentGameId = player.currentGameId;
    this.isSearchingForGame = player.isSearchingForGame;
  }

  public GetMyInfoResponse(boolean result, String username, Integer rating, Integer currentGameId, boolean isSearchingForGame)
  {
    super(result);
    this.username = username;
    this.rating = rating;
    this.currentGameId = currentGameId;
    this.isSearchingForGame = isSearchingForGame;
  }

}
