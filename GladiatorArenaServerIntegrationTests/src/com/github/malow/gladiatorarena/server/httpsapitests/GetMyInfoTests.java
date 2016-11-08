package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.testhelpers.ServerConnection;
import com.github.malow.gladiatorarena.server.testhelpers.TestHelpers;
import com.github.malow.gladiatorarena.server.testhelpers.TestUsers;
import com.github.malow.malowlib.GsonSingleton;

public class GetMyInfoTests
{
  @Before
  public void setup() throws Exception
  {
    TestHelpers.beforeTest();
  }

  @Test
  public void testGetMyInfoSuccessfully() throws Exception
  {
    String jsonResponse = ServerConnection.getMyInfo(TestUsers.USER1);
    GetMyInfoResponse response = GsonSingleton.get().fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentMatchId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(new Integer(0), response.rating);
    assertEquals(TestUsers.USER1.username, response.username);
  }

  @Test
  public void testGetMyInfoWithBadAuthToken() throws Exception
  {
    String jsonResponse = ServerConnection.getMyInfo(TestUsers.USER1.email, "BAD_TOKEN");
    ErrorResponse response = GsonSingleton.get().fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals("400: Bad Request", response.error);
  }

  @Test
  public void testGetMyInfoReutnsCorrectDataAfterSearchingForGame() throws Exception
  {
    ServerConnection.queueMatchmaking(TestUsers.USER1);
    String jsonResponse = ServerConnection.getMyInfo(TestUsers.USER1);
    GetMyInfoResponse response = GsonSingleton.get().fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentMatchId);
    assertEquals(true, response.isSearchingForGame);
    assertEquals(new Integer(0), response.rating);
    assertEquals(TestUsers.USER1.username, response.username);
  }

  @Test
  public void testGetMyInfoReutnsCorrectDataAfterFindingGame() throws Exception
  {
    ServerConnection.queueMatchmaking(TestUsers.USER1);
    ServerConnection.queueMatchmaking(TestUsers.USER2);
    String jsonResponse = ServerConnection.getMyInfo(TestUsers.USER1);
    GetMyInfoResponse response = GsonSingleton.get().fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNotNull(response.currentMatchId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(new Integer(0), response.rating);
    assertEquals(TestUsers.USER1.username, response.username);
  }
}
