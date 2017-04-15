package com.github.malow.gladiatorarena.server.database;

import com.github.malow.malowlib.database.Accessor;
import com.github.malow.malowlib.database.DatabaseConnection;

public class MatchAccessor extends Accessor<Match>
{
  public MatchAccessor(DatabaseConnection databaseConnection)
  {
    super(databaseConnection);
  }
}