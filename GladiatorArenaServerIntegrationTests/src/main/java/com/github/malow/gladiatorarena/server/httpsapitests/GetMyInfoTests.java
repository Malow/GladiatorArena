package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.malowlib.GsonSingleton;

public class GetMyInfoTests extends GladiatorArenaServerTestFixture
{
  @Test
  public void testGetMyInfoSuccessfully() throws Exception
  {
    ServerConnection.createUser(USER1);
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Double.valueOf(0.0), response.rating);
    assertEquals(USER1.username, response.username);
  }

  @Test
  public void testGetMyInfoWithoutUserCreated() throws Exception
  {
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.NO_USER_FOUND, response.error);
  }

  @Test
  public void testGetMyInfoWithBadAuthToken() throws Exception
  {
    ServerConnection.createUser(USER1);
    String jsonResponse = ServerConnection.getMyInfo(USER1.email, "BAD_TOKEN");
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals("400: Bad Request", response.error);
  }

  @Test
  public void testGetMyInfoReturnsCorrectDataAfterSearchingForGame() throws Exception
  {
    ServerConnection.createUser(USER1);
    ServerConnection.queueMatchmaking(USER1);
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameId);
    assertEquals(true, response.isSearchingForGame);
    assertEquals(Double.valueOf(0.0), response.rating);
    assertEquals(USER1.username, response.username);
  }

  @Test
  public void testGetMyInfoReturnsCorrectDataAfterFindingGame() throws Exception
  {
    ServerConnection.createUser(USER1);
    ServerConnection.createUser(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);
    ServerConnection.waitForEmptyMatchmakingEngine();
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNotNull(response.currentGameId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Double.valueOf(0.0), response.rating);
    assertEquals(USER1.username, response.username);
  }
}
