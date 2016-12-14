package com.github.malow.gladiatorarena.server.gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketGameFinishedUpdateRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketGameStateUpdateRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketJoinGameRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.testhelpers.Config;
import com.github.malow.gladiatorarena.server.testhelpers.ServerConnection;
import com.github.malow.gladiatorarena.server.testhelpers.TestHelpers;
import com.github.malow.gladiatorarena.server.testhelpers.TestUsers;
import com.github.malow.gladiatorarena.server.testhelpers.User;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWProcess;
import com.github.malow.malowlib.network.NetworkChannel;
import com.github.malow.malowlib.network.NetworkPacket;

public class GameTest
{
  @Before
  public void setup() throws Exception
  {
    TestHelpers.beforeTest();
  }

  @Test
  public void test() throws Exception
  {
    ServerConnection.queueMatchmaking(TestUsers.USER1);
    ServerConnection.queueMatchmaking(TestUsers.USER2);
    long matchId = GsonSingleton.fromJson(ServerConnection.getMyInfo(TestUsers.USER1), GetMyInfoResponse.class).currentMatchId;
    GameSocketClient p1 = new GameSocketClient(TestUsers.USER1, matchId);
    p1.start();
    GameSocketClient p2 = new GameSocketClient(TestUsers.USER2, matchId);
    p2.start();
    p1.waitUntillDone();
    p2.waitUntillDone();

    String jsonResponse = ServerConnection.getMyInfo(TestUsers.USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentMatchId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Integer.valueOf(-100), response.rating);
    assertEquals(TestUsers.USER1.username, response.username);

    jsonResponse = ServerConnection.getMyInfo(TestUsers.USER2);
    response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentMatchId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Integer.valueOf(100), response.rating);
    assertEquals(TestUsers.USER2.username, response.username);
  }

  private static class GameSocketClient extends MaloWProcess
  {
    private Long gameId;
    private String authToken;
    private String email;
    private NetworkChannel server;

    public GameSocketClient(User user, Long gameId)
    {
      this.email = user.email;
      this.authToken = user.authToken;
      this.gameId = gameId;
      this.server = new NetworkChannel(Config.GAME_SOCKET_SERVER_IP, Config.GAME_SOCKET_SERVER_PORT);
      this.server.setNotifier(this);
      this.server.start();
      this.server.sendData(GsonSingleton
          .toJson(new SocketJoinGameRequest(GladiatorArenaServerConfig.JOIN_GAME_REQUEST_NAME, this.email, this.authToken, this.gameId)));
    }

    @Override
    public void life()
    {
      NetworkPacket packet = (NetworkPacket) this.waitEvent();
      SocketResponse response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(GladiatorArenaServerConfig.JOIN_GAME_REQUEST_NAME, response.method);

      this.server.sendData(GsonSingleton.toJson(new SocketRequest(GladiatorArenaServerConfig.READY_REQUEST_NAME)));
      packet = (NetworkPacket) this.waitEvent();
      response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(GladiatorArenaServerConfig.READY_REQUEST_NAME, response.method);

      packet = (NetworkPacket) this.waitEvent();
      SocketGameStateUpdateRequest updateRequest = GsonSingleton.fromJson(packet.getMessage(), SocketGameStateUpdateRequest.class);
      assertEquals(GladiatorArenaServerConfig.GAME_STATE_UPDATE_REQUEST_NAME, updateRequest.method);
      assertEquals("test", updateRequest.test);
      this.server.sendData(GsonSingleton.toJson(new SocketResponse(GladiatorArenaServerConfig.GAME_STATE_UPDATE_REQUEST_NAME, true)));

      this.server.sendData(GsonSingleton.toJson(new SocketRequest(GladiatorArenaServerConfig.READY_REQUEST_NAME)));
      packet = (NetworkPacket) this.waitEvent();
      response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(GladiatorArenaServerConfig.READY_REQUEST_NAME, response.method);

      packet = (NetworkPacket) this.waitEvent();
      SocketGameFinishedUpdateRequest finishRequest = GsonSingleton.fromJson(packet.getMessage(), SocketGameFinishedUpdateRequest.class);
      assertEquals(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, finishRequest.method);
      assertEquals(TestUsers.USER2.username, finishRequest.winnerUsername);
      this.server.sendData(GsonSingleton.toJson(new SocketResponse(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, true)));
    }

    @Override
    public void closeSpecific()
    {
    }

  }
}
