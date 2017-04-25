package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.malowlib.GsonSingleton;

public class QueueMatchmakingTests extends GladiatorArenaServerTestFixture
{
  @Test
  public void testQueueMatchmakingSuccessfully() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    String jsonResponse = ServerConnection.queueMatchmaking(USER1);
    Response response = GsonSingleton.fromJson(jsonResponse, Response.class);
    assertEquals(true, response.result);
  }

  @Test
  public void testQueueMatchmakingFailsWhenAlreadyInQueue() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    ServerConnection.queueMatchmaking(USER1);
    String jsonResponse = ServerConnection.queueMatchmaking(USER1);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ALREADY_IN_QUEUE, response.error);
  }

  @Test
  public void testQueueMatchmakingFailsWhenAlreadyHaveOngoingMatch() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    ServerConnection.createPlayer(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);

    String jsonResponse = ServerConnection.queueMatchmaking(USER1);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ALREADY_HAVE_A_MATCH, response.error);
  }
}
