package com.github.malow.gladiatorarena.server;

import org.junit.Before;

import com.github.malow.accountserver.comstructs.account.LoginResponse;
import com.github.malow.accountserver.database.Account;
import com.github.malow.accountserver.database.AccountAccessor;
import com.github.malow.gladiatorarena.server.database.MatchAccessor;
import com.github.malow.gladiatorarena.server.database.MatchReferenceAccessor;
import com.github.malow.gladiatorarena.server.database.PlayerAccessor;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseConnection.DatabaseType;

public class GladiatorArenaServerTestFixture
{
  public static class TestUser
  {
    public String email;
    public String username;
    public String password;
    public String authToken;

    public TestUser(String email, String username, String password, String authToken)
    {
      this.email = email;
      this.username = username;
      this.password = password;
      this.authToken = authToken;
    }
  }

  public static final TestUser USER1 = new TestUser("tester1@test.com", "tester1", "testerpw", null);
  public static final TestUser USER2 = new TestUser("tester2@test.com", "tester2", "testerpw", null);


  private static AccountAccessor accountAccessor = new AccountAccessor(
      DatabaseConnection.get(DatabaseType.SQLITE_FILE, "../GladiatorArenaServer/GladiatorArena"));
  private static PlayerAccessor playerAccessor = new PlayerAccessor(
      DatabaseConnection.get(DatabaseType.SQLITE_FILE, "../GladiatorArenaServer/GladiatorArena"));
  private static MatchAccessor matchAccessor = new MatchAccessor(
      DatabaseConnection.get(DatabaseType.SQLITE_FILE, "../GladiatorArenaServer/GladiatorArena"));
  private static MatchReferenceAccessor matchReferenceAccessor = new MatchReferenceAccessor(
      DatabaseConnection.get(DatabaseType.SQLITE_FILE, "../GladiatorArenaServer/GladiatorArena"));

  @Before
  public void beforeTest() throws Exception
  {
    this.resetDatabase();
    ServerConnection.clearCaches();

    this.seedAccountTable();

    USER1.authToken = this.loginAndGetAuthToken(USER1);
    USER2.authToken = this.loginAndGetAuthToken(USER2);
  }

  private void resetDatabase() throws Exception
  {
    matchReferenceAccessor.createTable();
    matchAccessor.createTable();
    playerAccessor.createTable();
    accountAccessor.createTable();
  }

  private void seedAccountTable() throws Exception
  {
    Account user1 = new Account();
    user1.email = USER1.email;
    user1.password = "$2a$08$FwfADf4UV2oaQ75xMRHKZO/0ETEB5asMk63YquyAtv1GjnxW1aKqC";
    accountAccessor.create(user1);
    Account user2 = new Account();
    user2.email = USER2.email;
    user2.password = "$2a$08$FwfADf4UV2oaQ75xMRHKZO/0ETEB5asMk63YquyAtv1GjnxW1aKqC";
    accountAccessor.create(user2);
  }

  public String loginAndGetAuthToken(TestUser user) throws Exception
  {
    String jsonResponse = ServerConnection.login(user);
    LoginResponse response = GsonSingleton.fromJson(jsonResponse, LoginResponse.class);
    return response.authToken;
  }
}
