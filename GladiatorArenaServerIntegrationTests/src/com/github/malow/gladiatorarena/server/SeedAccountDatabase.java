package com.github.malow.gladiatorarena.server;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

public class SeedAccountDatabase
{
  @Test
  public void seedAccountDatabase() throws Exception
  {
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost?user=GladArUsr&password=password&autoReconnect=true");
    runSqlStatementsFromFile(connection, "SeedAccountDatabase.sql");
  }

  private static void runSqlStatementsFromFile(Connection connection, String pathToFile) throws Exception
  {
    String file = new String(Files.readAllBytes(Paths.get(pathToFile)));
    String[] statements = file.split("\\;");
    for (String statement : statements)
    {
      try
      {
        connection.prepareStatement(statement + ";").execute();
      }
      catch (Exception e2)
      {
      }
    }
  }
}
