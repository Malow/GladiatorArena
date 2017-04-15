package com.github.malow.gladiatorarena.server.database;

import com.github.malow.malowlib.database.DatabaseConnection;

public enum MatchAccessorSingleton
{
  INSTANCE;

  private MatchAccessor accessor;

  public static MatchAccessor get()
  {
    return INSTANCE.accessor;
  }

  public static void init(DatabaseConnection databaseConnection)
  {
    INSTANCE.accessor = new MatchAccessor(databaseConnection);
  }
}
