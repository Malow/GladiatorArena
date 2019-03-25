package com.github.malow.gladiatorarena.server;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.accountserver.comstructs.account.LoginRequest;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture.TestUser;
import com.github.malow.gladiatorarena.server.comstructs.CreateUserRequest;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.SimpleJsonHttpClient;

public class ServerConnection
{
  public static final SimpleJsonHttpClient httpsClient = new SimpleJsonHttpClient(Config.HTTPS_SERVER_HOST, true);

  public static String login(TestUser user) throws Exception
  {
    return httpsClient.sendPost("/account/login", new LoginRequest(user.email, user.password));
  }

  public static String createUser(TestUser user) throws Exception
  {
    return createUser(user.email, user.authToken, user.username);
  }

  public static String createUser(String email, String authToken, String username) throws Exception
  {
    return httpsClient.sendPost("/createuser", new CreateUserRequest(email, authToken, username));
  }

  public static String getMyInfo(TestUser user) throws Exception
  {
    return getMyInfo(user.email, user.authToken);
  }

  public static String getMyInfo(String email, String authToken) throws Exception
  {
    return httpsClient.sendPost("/getmyinfo", new AuthorizedRequest(email, authToken));
  }

  public static String queueMatchmaking(TestUser user) throws Exception
  {
    return httpsClient.sendPost("/queuematchmaking", new AuthorizedRequest(user.email, user.authToken));
  }

  public static String unqueueMatchmaking(TestUser user) throws Exception
  {
    return httpsClient.sendPost("/unqueuematchmaking", new AuthorizedRequest(user.email, user.authToken));
  }

  public static void clearCaches() throws Exception
  {
    httpsClient.sendPost("/account/clearcache", "{}");
    httpsClient.sendPost("/clearcache", "{}");
  }

  public static void waitForEmptyMatchmakingEngine() throws Exception
  {
    String json = httpsClient.sendPost("/waitforemptymatchmakingengine", "{}");
    Response response = GsonSingleton.fromJson(json, Response.class);
    if (!response.result)
    {
      throw new RuntimeException("waitForEmptyMatchmakingEngine timed out");
    }
  }
}
