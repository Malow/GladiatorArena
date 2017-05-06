package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.malowlib.GsonSingleton;

public class UnqueueMatchmakingTests extends GladiatorArenaServerTestFixture
{
  @Test
  public void testUnqueueMatchmakingSuccessfully() throws Exception
  {
    ServerConnection.createUser(USER1);
    ServerConnection.queueMatchmaking(USER1);
    String jsonResponse = ServerConnection.unqueueMatchmaking(USER1);
    Response response = GsonSingleton.fromJson(jsonResponse, Response.class);
    assertEquals(true, response.result);
  }

  @Test
  public void testUnqueueMatchmakingFailsWhenNotQueued() throws Exception
  {
    ServerConnection.createUser(USER1);
    String jsonResponse = ServerConnection.unqueueMatchmaking(USER1);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.NOT_IN_QUEUE, response.error);
  }

  @Test
  public void testUnqueueMatchmakingFailsWhenAlreadyHaveOngoingMatch() throws Exception
  {
    ServerConnection.createUser(USER1);
    ServerConnection.createUser(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);

    String jsonResponse = ServerConnection.unqueueMatchmaking(USER1);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ALREADY_HAVE_A_MATCH, response.error);
  }
}
