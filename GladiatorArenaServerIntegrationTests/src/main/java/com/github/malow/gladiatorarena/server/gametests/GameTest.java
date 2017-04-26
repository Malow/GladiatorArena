package com.github.malow.gladiatorarena.server.gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.malow.gladiatorarena.server.Config;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.ActionRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.GameFinishedUpdate;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.GameStateUpdate;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.JoinGameRequest;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.network.NetworkChannel;
import com.github.malow.malowlib.network.NetworkPacket;

public class GameTest extends GladiatorArenaServerTestFixture
{
  @Test
  public void test() throws Exception
  {
    ServerConnection.createPlayer(USER1);
    ServerConnection.createPlayer(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);

    Thread.sleep(1000);

    Integer matchId = GsonSingleton.fromJson(ServerConnection.getMyInfo(USER1), GetMyInfoResponse.class).currentGameId;
    GameSocketClient p1 = new GameSocketClient(USER1, matchId);
    p1.start();
    GameSocketClient p2 = new GameSocketClient(USER2, matchId);
    p2.start();
    p1.waitUntillDone();
    p2.waitUntillDone();

    Thread.sleep(1000);

    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Integer.valueOf(100), response.rating);
    assertEquals(USER1.username, response.username);

    jsonResponse = ServerConnection.getMyInfo(USER2);
    response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameId);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Integer.valueOf(-100), response.rating);
    assertEquals(USER2.username, response.username);
  }

  private static class GameSocketClient extends MaloWProcess
  {
    private Integer gameId;
    private String authToken;
    private String email;
    private NetworkChannel server;

    public GameSocketClient(TestUser user, Integer gameId)
    {
      this.email = user.email;
      this.authToken = user.authToken;
      this.gameId = gameId;
      this.server = new NetworkChannel(Config.GAME_SOCKET_SERVER_IP, Config.GAME_SOCKET_SERVER_PORT);
      this.server.setNotifier(this);
      this.server.start();
      this.server.sendData(GsonSingleton.toJson(new JoinGameRequest(this.email, this.authToken, this.gameId)));
    }

    @Override
    public void life()
    {
      NetworkPacket packet = (NetworkPacket) this.waitEvent();
      SocketResponse response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(MethodNames.JOIN_GAME_REQUEST, response.method);

      this.server.sendData(GsonSingleton.toJson(new SocketMessage(MethodNames.READY)));
      packet = (NetworkPacket) this.waitEvent();
      response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(MethodNames.READY, response.method);

      packet = (NetworkPacket) this.waitEvent();
      GameStateUpdate updateRequest = GsonSingleton.fromJson(packet.getMessage(), GameStateUpdate.class);
      assertEquals(MethodNames.GAME_STATE_UPDATE, updateRequest.method);
      this.server.sendData(GsonSingleton.toJson(new SocketResponse(MethodNames.GAME_STATE_UPDATE, true)));

      this.server.sendData(GsonSingleton.toJson(new SocketMessage(MethodNames.READY)));
      packet = (NetworkPacket) this.waitEvent();
      response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(MethodNames.READY, response.method);

      this.server.sendData(GsonSingleton.toJson(new ActionRequest()));
      packet = (NetworkPacket) this.waitEvent();
      response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);

      packet = (NetworkPacket) this.waitEvent();
      GameFinishedUpdate finishRequest = GsonSingleton.fromJson(packet.getMessage(), GameFinishedUpdate.class);
      assertEquals(MethodNames.GAME_FINISHED_UPDATE, finishRequest.method);
      assertEquals(USER1.username, finishRequest.winnerUsername);
      this.server.sendData(GsonSingleton.toJson(new SocketResponse(MethodNames.GAME_FINISHED_UPDATE, true)));
    }

    @Override
    public void closeSpecific()
    {
    }
  }
}
