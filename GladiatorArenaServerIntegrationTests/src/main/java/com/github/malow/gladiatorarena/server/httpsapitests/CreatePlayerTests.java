package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.testhelpers.ServerConnection;
import com.github.malow.malowlib.GsonSingleton;

public class CreatePlayerTests extends GladiatorArenaServerTestFixture
{
  @Test
  public void testCreatePlayerSuccessfully() throws Exception
  {
    String jsonResponse = ServerConnection.createPlayer(USER1);
    Response response = GsonSingleton.fromJson(jsonResponse, Response.class);
    assertEquals(true, response.result);
  }

  @Test
  public void testCreateTwoPlayersWithSameUsername() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    String jsonResponse = ServerConnection.createPlayer(USER2.email, USER2.authToken, USER1.username);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.USERNAME_TAKEN, response.error);
  }

  @Test
  public void testCreateTwoPlayersOnSameAccount() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    String jsonResponse = ServerConnection.createPlayer(USER1.email, USER1.authToken, "SecondPlayer");
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ACCOUNT_ALREADY_HAS_PLAYER, response.error);
  }
}
