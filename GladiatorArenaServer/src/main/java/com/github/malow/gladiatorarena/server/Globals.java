package com.github.malow.gladiatorarena.server;

import com.github.malow.gladiatorarena.server.database.MatchAccessor;
import com.github.malow.gladiatorarena.server.database.PlayerAccessor;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseConnection.DatabaseType;

public class Globals
{
  public static PlayerAccessor playerAccessor = new PlayerAccessor(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"));
  public static MatchAccessor matchAccessor = new MatchAccessor(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"));
}
