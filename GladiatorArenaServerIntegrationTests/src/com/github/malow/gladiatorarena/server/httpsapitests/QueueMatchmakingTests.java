package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.testhelpers.ServerConnection;
import com.github.malow.gladiatorarena.server.testhelpers.TestHelpers;
import com.github.malow.gladiatorarena.server.testhelpers.TestUsers;
import com.github.malow.malowlib.GsonSingleton;

public class QueueMatchmakingTests
{
  @Before
  public void setup() throws Exception
  {
    TestHelpers.beforeTest();
  }

  @Test
  public void testQueueMatchmakingSuccessfully() throws Exception
  {
    String jsonResponse = ServerConnection.queueMatchmaking(TestUsers.USER1);
    Response response = GsonSingleton.get().fromJson(jsonResponse, Response.class);
    assertEquals(true, response.result);
  }

  @Test
  public void testQueueMatchmakingFailsWhenAlreadyInQueue() throws Exception
  {
    ServerConnection.queueMatchmaking(TestUsers.USER1);
    String jsonResponse = ServerConnection.queueMatchmaking(TestUsers.USER1);
    ErrorResponse response = GsonSingleton.get().fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ALREADY_IN_QUEUE, response.error);
  }

  @Test
  public void testQueueMatchmakingFailsWhenAlreadyHaveOngoingMatch() throws Exception
  {
    ServerConnection.queueMatchmaking(TestUsers.USER1);
    ServerConnection.queueMatchmaking(TestUsers.USER2);

    String jsonResponse = ServerConnection.queueMatchmaking(TestUsers.USER1);
    ErrorResponse response = GsonSingleton.get().fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ALREADY_HAVE_A_MATCH, response.error);
  }
}