package com.github.malow.gladiatorarena.server.database;

import com.github.malow.malowlib.database.DatabaseConnection;

public enum MatchReferenceAccessorSingleton
{
  INSTANCE;

  private MatchReferenceAccessor accessor;

  public static MatchReferenceAccessor get()
  {
    return INSTANCE.accessor;
  }

  public static void init(DatabaseConnection databaseConnection)
  {
    INSTANCE.accessor = new MatchReferenceAccessor(databaseConnection);
  }
}
