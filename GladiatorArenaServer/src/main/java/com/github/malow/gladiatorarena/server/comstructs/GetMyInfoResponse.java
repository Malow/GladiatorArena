package com.github.malow.gladiatorarena.server.comstructs;

import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.Player;

public class GetMyInfoResponse extends Response
{
  public String username;
  public Integer rating;
  public Integer currentMatchId;
  public boolean isSearchingForGame;

  public GetMyInfoResponse(boolean result, Player player)
  {
    super(result);
    this.username = player.username;
    this.rating = player.rating;
    this.currentMatchId = player.currentGameId;
    this.isSearchingForGame = player.isSearchingForGame;
  }

  public GetMyInfoResponse(boolean result, String username, Integer rating, Integer currentMatchId, boolean isSearchingForGame)
  {
    super(result);
    this.username = username;
    this.rating = rating;
    this.currentMatchId = currentMatchId;
    this.isSearchingForGame = isSearchingForGame;
  }

}
