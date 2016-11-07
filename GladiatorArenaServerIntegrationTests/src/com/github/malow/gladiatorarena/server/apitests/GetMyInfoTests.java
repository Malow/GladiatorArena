package com.github.malow.gladiatorarena.server.apitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.testhelpers.ServerConnection;
import com.github.malow.gladiatorarena.server.testhelpers.TestHelpers;
import com.github.malow.gladiatorarena.server.testhelpers.User;
import com.github.malow.malowlib.GsonSingleton;

public class GetMyInfoTests
{
  private static final User TEST_USER = new User("tester@test.com", "tester", "testerpw", null);

  @Before
  public void setup() throws Exception
  {
    TestHelpers.beforeTest();
    TEST_USER.authToken = TestHelpers.loginAndGetAuthToken(TEST_USER);
  }

  @Test
  public void testGetMyInfoSuccessfully() throws Exception
  {
    String jsonResponse = ServerConnection.getMyInfo(TEST_USER);
    GetMyInfoResponse response = GsonSingleton.get().fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(response.result, true);
    assertNull(response.currentMatchId);
    assertEquals(response.isSearchingForGame, false);
    assertEquals(response.rating, new Integer(0));
    assertEquals(response.username, TEST_USER.username);
  }

  @Test
  public void testGetMyInfoWithBadAuthToken() throws Exception
  {
    String jsonResponse = ServerConnection.getMyInfo(TEST_USER.email, "BAD_TOKEN");
    ErrorResponse response = GsonSingleton.get().fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(response.result, false);
    assertEquals(response.error, "400: Bad Request");
  }
}
