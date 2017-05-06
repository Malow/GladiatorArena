package com.github.malow.gladiatorarena.server.database;

import com.github.malow.malowlib.database.DatabaseConnection;

public enum UserAccessorSingleton
{
  INSTANCE;

  private UserAccessor accessor;

  public static UserAccessor get()
  {
    return INSTANCE.accessor;
  }

  public static void init(DatabaseConnection databaseConnection)
  {
    INSTANCE.accessor = new UserAccessor(databaseConnection);
  }
}
