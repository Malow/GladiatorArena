package com.github.malow.gladiatorarena.server.testhelpers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.account.LoginRequest;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.HttpsPostClient;

public class ServerConnection
{
  public static final HttpsPostClient accountServerClient = new HttpsPostClient(Config.ACCOUNT_SERVER_HOST, true);
  public static final HttpsPostClient gameServerClient = new HttpsPostClient(Config.GAME_SERVER_HOST, true);

  public static String login(User user) throws Exception
  {
    String request = GsonSingleton.toJson(new LoginRequest(user.email, user.password));
    return accountServerClient.sendMessage("/login", request);
  }

  public static String getMyInfo(User user) throws Exception
  {
    return getMyInfo(user.email, user.authToken);
  }

  public static String getMyInfo(String email, String authToken) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(email, authToken));
    return gameServerClient.sendMessage("/getmyinfo", request);
  }

  public static String queueMatchmaking(User user) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(user.email, user.authToken));
    return gameServerClient.sendMessage("/queuematchmaking", request);
  }

  public static String unqueueMatchmaking(User user) throws Exception
  {
    String request = GsonSingleton.toJson(new AuthorizedRequest(user.email, user.authToken));
    return gameServerClient.sendMessage("/unqueuematchmaking", request);
  }

  public static void clearCache() throws Exception
  {
    gameServerClient.sendMessage("/clearcache", "");
  }
}
