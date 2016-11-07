package com.github.malow.gladiatorarena.server.apitests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.testhelpers.ServerConnection;
import com.github.malow.gladiatorarena.server.testhelpers.TestHelpers;
import com.github.malow.gladiatorarena.server.testhelpers.User;
import com.github.malow.malowlib.GsonSingleton;

public class UnqueueMatchmakingTests
{
  private static final User TEST_USER = new User("tester@test.com", null, "testerpw", null);

  @Before
  public void setup() throws Exception
  {
    TestHelpers.beforeTest();
    TEST_USER.authToken = TestHelpers.loginAndGetAuthToken(TEST_USER);
    ServerConnection.getMyInfo(TEST_USER);
  }

  @Test
  public void testUnqueueMatchmakingSuccessfully() throws Exception
  {
    ServerConnection.queueMatchmaking(TEST_USER);
    String jsonResponse = ServerConnection.unqueueMatchmaking(TEST_USER);
    Response response = GsonSingleton.get().fromJson(jsonResponse, Response.class);
    assertEquals(response.result, true);
  }

  @Test
  public void testUnqueueMatchmakingFailsWhenNotQueued() throws Exception
  {
    String jsonResponse = ServerConnection.unqueueMatchmaking(TEST_USER);
    ErrorResponse response = GsonSingleton.get().fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(response.result, false);
    assertEquals(response.error, ErrorMessages.NOT_IN_QUEUE);
  }

  @Test
  public void testUnqueueMatchmakingFailsWhenAlreadyHaveOngoingMatch() throws Exception
  {
    ServerConnection.queueMatchmaking(TEST_USER);
    User user2 = new User("tester2@test.com", null, "testerpw", null);
    user2.authToken = TestHelpers.loginAndGetAuthToken(user2);
    ServerConnection.getMyInfo(user2);
    ServerConnection.queueMatchmaking(user2);

    String jsonResponse = ServerConnection.unqueueMatchmaking(TEST_USER);
    ErrorResponse response = GsonSingleton.get().fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(response.result, false);
    assertEquals(response.error, ErrorMessages.ALREADY_HAVE_A_MATCH);
  }
}
