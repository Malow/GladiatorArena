package com.github.malow.gladiatorarena.server.gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.malowlib.GsonSingleton;

public class GameTest extends GladiatorArenaServerTestFixture
{
  @Test
  public void test() throws Exception
  {
    ServerConnection.createUser(USER1);
    ServerConnection.createUser(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);

    ServerConnection.waitForEmptyMatchmakingEngine();

    String gameToken1 = GsonSingleton.fromJson(ServerConnection.getMyInfo(USER1), GetMyInfoResponse.class).currentGameToken;
    TestGameClient p1 = new TestGameClient(USER1.username, USER2.username, gameToken1, true);
    p1.start();

    Thread.sleep(100);

    String gameToken2 = GsonSingleton.fromJson(ServerConnection.getMyInfo(USER2), GetMyInfoResponse.class).currentGameToken;
    TestGameClient p2 = new TestGameClient(USER2.username, USER1.username, gameToken2, false);
    p2.start();

    p1.waitUntillDone();
    p2.waitUntillDone();

    Thread.sleep(1000);

    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameToken);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Double.valueOf(100.0), response.rating);
    assertEquals(USER1.username, response.username);

    jsonResponse = ServerConnection.getMyInfo(USER2);
    response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameToken);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Double.valueOf(-100.0), response.rating);
    assertEquals(USER2.username, response.username);
  }
}
