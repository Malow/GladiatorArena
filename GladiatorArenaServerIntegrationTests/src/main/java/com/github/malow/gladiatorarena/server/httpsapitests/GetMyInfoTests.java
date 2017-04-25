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
    ServerConnection.createPlayer(USER1);
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentMatchId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Integer.valueOf(0), response.rating);
    assertEquals(USER1.username, response.username);
  }

  @Test
  public void testGetMyInfoWithoutPlayerCreated() throws Exception
  {
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.NO_PLAYER_FOUND, response.error);
  }

  @Test
  public void testGetMyInfoWithBadAuthToken() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    String jsonResponse = ServerConnection.getMyInfo(USER1.email, "BAD_TOKEN");
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals("400: Bad Request", response.error);
  }

  @Test
  public void testGetMyInfoReturnsCorrectDataAfterSearchingForGame() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    ServerConnection.queueMatchmaking(USER1);
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentMatchId);
    assertEquals(true, response.isSearchingForGame);
    assertEquals(Integer.valueOf(0), response.rating);
    assertEquals(USER1.username, response.username);
  }

  @Test
  public void testGetMyInfoReutnsCorrectDataAfterFindingGame() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    ServerConnection.createPlayer(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);
    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNotNull(response.currentMatchId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Integer.valueOf(0), response.rating);
    assertEquals(USER1.username, response.username);
  }
}
