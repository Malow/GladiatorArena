package com.github.malow.gladiatorarena.server.database;

import com.github.malow.accountserver.database.Account;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;

public class Player
{
  // Persisted in database
  public Long id;
  public Long accountId;
  public String username;
  public Integer rating;

  // Only cached in memory
  public Long currentMatchId;
  public boolean isSearchingForGame;

  public Player()
  {

  }

  public Player(Account acc)
  {
    this.accountId = acc.id;
    this.username = acc.username;
    this.rating = GladiatorArenaServerConfig.DEFAULT_RATING;
  }

  public Player(Long id, Long accountId, String username, Integer rating)
  {
    this.id = id;
    this.accountId = accountId;
    this.username = username;
    this.rating = rating;
  }

}
