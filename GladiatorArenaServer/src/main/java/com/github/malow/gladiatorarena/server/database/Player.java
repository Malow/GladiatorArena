package com.github.malow.gladiatorarena.server.database;

import com.github.malow.accountserver.database.Account;
import com.github.malow.malowlib.database.DatabaseTableEntity;

public class Player extends DatabaseTableEntity
{
  @Unique
  @ForeignKey(target = Account.class)
  public Integer accountId;
  @Unique
  public String username;
  public Integer rating = 0;

  // Only cached in memory
  @NotPersisted
  public Integer currentMatchId = null;
  @NotPersisted
  public boolean isSearchingForGame = false;
}
