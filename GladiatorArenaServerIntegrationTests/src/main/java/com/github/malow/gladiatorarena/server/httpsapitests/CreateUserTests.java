package com.github.malow.gladiatorarena.server.httpsapitests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.malowlib.GsonSingleton;

public class CreateUserTests extends GladiatorArenaServerTestFixture
{
  @Test
  public void testCreateUserSuccessfully() throws Exception
  {
    String jsonResponse = ServerConnection.createUser(USER1);
    Response response = GsonSingleton.fromJson(jsonResponse, Response.class);
    assertEquals(true, response.result);
  }

  @Test
  public void testCreateTwoUsersWithSameUsername() throws Exception
  {
    ServerConnection.createUser(USER1);
    String jsonResponse = ServerConnection.createUser(USER2.email, USER2.authToken, USER1.username);
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.USERNAME_TAKEN, response.error);
  }

  @Test
  public void testCreateTwoUsersOnSameAccount() throws Exception
  {
    ServerConnection.createUser(USER1);
    String jsonResponse = ServerConnection.createUser(USER1.email, USER1.authToken, "SecondUser");
    ErrorResponse response = GsonSingleton.fromJson(jsonResponse, ErrorResponse.class);
    assertEquals(false, response.result);
    assertEquals(ErrorMessages.ACCOUNT_ALREADY_HAS_USER, response.error);
  }
}
