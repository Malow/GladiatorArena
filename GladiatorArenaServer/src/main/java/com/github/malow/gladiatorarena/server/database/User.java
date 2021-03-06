package com.github.malow.gladiatorarena.server.database;

import com.github.malow.accountserver.database.Account;
import com.github.malow.malowlib.database.DatabaseTableEntity;

public class User extends DatabaseTableEntity
{
  @Unique
  @ForeignKey(target = Account.class)
  public Integer accountId;
  @Unique
  public String username;
  public Double rating = 0.0;

  // Only cached in memory
  @NotPersisted
  public String currentGameToken = null;
  @NotPersisted
  public boolean isSearchingForGame = false;
}
