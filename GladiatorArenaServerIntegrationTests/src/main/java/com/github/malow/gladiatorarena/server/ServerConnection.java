package com.github.malow.gladiatorarena.server;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.accountserver.comstructs.account.LoginRequest;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture.TestUser;
import com.github.malow.gladiatorarena.server.comstructs.CreateUserRequest;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.HttpsPostClient;

public class ServerConnection
{
  public static final HttpsPostClient httpsClient = new HttpsPostClient(Config.HTTPS_SERVER_HOST, true);

  public static String login(TestUser user) throws Exception
  {
    String request = GsonSingleton.toJson(new LoginRequest(user.email, user.password));
    return httpsClient.sendMessage("/account/login", request);
  }

  public static String createUser(TestUser user) throws Exception
  {
    return createUser(user.email, user.authToken, user.username);
  }

  public static String createUser(String email, String authToken, String username) throws Exception
  {
    String request = GsonSingleton.toJson(new CreateUserRequest(email, authToken, username));
    return httpsClient.sendMessage("/createuser", request);
  }

  public static String getMyInfo(TestUser user) throws Exception
  {
    return getMyInfo(user.email, user.authToken);
  }

  public static String getMyInfo(String email, String authToken) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(email, authToken));
    return httpsClient.sendMessage("/getmyinfo", request);
  }

  public static String queueMatchmaking(TestUser user) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(user.email, user.authToken));
    return httpsClient.sendMessage("/queuematchmaking", request);
  }

  public static String unqueueMatchmaking(TestUser user) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(user.email, user.authToken));
    return httpsClient.sendMessage("/unqueuematchmaking", request);
  }

  public static void clearCaches() throws Exception
  {
    httpsClient.sendMessage("/account/clearcache", "{}");
    httpsClient.sendMessage("/clearcache", "{}");
  }

  public static void waitForEmptyMatchmakingEngine() throws Exception
  {
    String json = httpsClient.sendMessage("/waitforemptymatchmakingengine", "{}");
    Response response = GsonSingleton.fromJson(json, Response.class);
    if (!response.result)
    {
      throw new RuntimeException("waitForEmptyMatchmakingEngine timed out");
    }
  }
}
