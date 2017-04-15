package com.github.malow.gladiatorarena.server.database;

import com.github.malow.malowlib.database.DatabaseConnection;

public enum PlayerAccessorSingleton
{
  INSTANCE;

  private PlayerAccessor accessor;

  public static PlayerAccessor get()
  {
    return INSTANCE.accessor;
  }

  public static void init(DatabaseConnection databaseConnection)
  {
    INSTANCE.accessor = new PlayerAccessor(databaseConnection);
  }
}
