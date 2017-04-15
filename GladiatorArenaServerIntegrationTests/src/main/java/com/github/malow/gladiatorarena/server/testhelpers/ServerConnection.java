package com.github.malow.gladiatorarena.server.testhelpers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.account.LoginRequest;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture.TestUser;
import com.github.malow.gladiatorarena.server.comstructs.CreatePlayerRequest;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.HttpsPostClient;

public class ServerConnection
{
  public static final HttpsPostClient accountServerClient = new HttpsPostClient(Config.ACCOUNT_SERVER_HOST, true);
  public static final HttpsPostClient gameServerClient = new HttpsPostClient(Config.GAME_SERVER_HOST, true);

  public static String login(TestUser user) throws Exception
  {
    String request = GsonSingleton.toJson(new LoginRequest(user.email, user.password));
    return accountServerClient.sendMessage("/login", request);
  }

  public static String createPlayer(TestUser user) throws Exception
  {
    return createPlayer(user.email, user.authToken, user.username);
  }

  public static String createPlayer(String email, String authToken, String username) throws Exception
  {
    String request = GsonSingleton.toJson(new CreatePlayerRequest(email, authToken, username));
    return gameServerClient.sendMessage("/createplayer", request);
  }

  public static String getMyInfo(TestUser user) throws Exception
  {
    return getMyInfo(user.email, user.authToken);
  }

  public static String getMyInfo(String email, String authToken) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(email, authToken));
    return gameServerClient.sendMessage("/getmyinfo", request);
  }

  public static String queueMatchmaking(TestUser user) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(user.email, user.authToken));
    return gameServerClient.sendMessage("/queuematchmaking", request);
  }

  public static String unqueueMatchmaking(TestUser user) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(user.email, user.authToken));
    return gameServerClient.sendMessage("/unqueuematchmaking", request);
  }

  public static void clearCache() throws Exception
  {
    gameServerClient.sendMessage("/clearcache", "");
  }
}
