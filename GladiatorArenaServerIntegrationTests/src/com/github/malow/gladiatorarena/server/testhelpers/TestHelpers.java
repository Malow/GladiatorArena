package com.github.malow.gladiatorarena.server.testhelpers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import com.github.malow.accountserver.comstructs.account.LoginResponse;
import com.github.malow.malowlib.GsonSingleton;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

public class TestHelpers
{
  public static void beforeTest() throws Exception
  {
    resetDatabaseTable("players");
    resetDatabaseTable("matches");
    ServerConnection.clearCache();
  }

  public static String loginAndGetAuthToken(User user) throws Exception
  {
    String jsonResponse = ServerConnection.login(user);
    LoginResponse response = GsonSingleton.get().fromJson(jsonResponse, LoginResponse.class);
    return response.authToken;
  }

  public static void resetDatabaseTable(String tableName) throws Exception
  {
    try
    {
      doResetDatabaseTable(tableName);
    }
    catch (MySQLNonTransientConnectionException e) // Database probably doesn't exist, create it.
    {
      createDatabase();
    }
    doResetDatabaseTable(tableName);
  }

  private static void doResetDatabaseTable(String tableName) throws Exception
  {
    Connection connection = DriverManager
        .getConnection("jdbc:mysql://localhost/GladiatorArenaServer?" + "user=GladArUsr&password=password&autoReconnect=true");
    String sql = "DELETE FROM " + tableName + " ;";
    PreparedStatement s1 = connection.prepareStatement(sql);
    s1.execute();
  }

  private static void createDatabase() throws Exception
  {
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost?" + "user=GladArUsr&password=password&autoReconnect=true");
    runSqlStatementsFromFile(connection, "../CreateMysqlDatabase.sql");
    runSqlStatementsFromFile(connection, "../CreateSqlTables.sql");
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
        throw e2;
      }
    }
  }
}
