package com.github.malow.gladiatorarena.server.gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.malow.gladiatorarena.gamecore.hex.Position;
import com.github.malow.gladiatorarena.gamecore.message.AttackAction;
import com.github.malow.gladiatorarena.gamecore.message.GameFinishedUpdate;
import com.github.malow.gladiatorarena.gamecore.message.GameStateUpdate;
import com.github.malow.gladiatorarena.gamecore.message.MoveAction;
import com.github.malow.gladiatorarena.gamecore.message.UnitData;
import com.github.malow.gladiatorarena.server.Config;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerTestFixture;
import com.github.malow.gladiatorarena.server.ServerConnection;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.JoinGameRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage.SocketMethod;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.network.NetworkChannel;
import com.github.malow.malowlib.network.NetworkPacket;

public class GameTest extends GladiatorArenaServerTestFixture
{
  @Test
  public void test() throws Exception
  {
    ServerConnection.createUser(USER1);
    ServerConnection.createUser(USER2);
    ServerConnection.queueMatchmaking(USER1);
    ServerConnection.queueMatchmaking(USER2);

    ServerConnection.waitForEmptyMatchmakingEngine();

    String gameToken1 = GsonSingleton.fromJson(ServerConnection.getMyInfo(USER1), GetMyInfoResponse.class).currentGameToken;
    GameSocketClient p1 = new GameSocketClient(USER1, gameToken1);
    String gameToken2 = GsonSingleton.fromJson(ServerConnection.getMyInfo(USER2), GetMyInfoResponse.class).currentGameToken;
    GameSocketClient p2 = new GameSocketClient(USER2, gameToken2);
    p1.start();
    p2.start();
    p1.waitUntillDone();
    p2.waitUntillDone();

    Thread.sleep(1000);

    String jsonResponse = ServerConnection.getMyInfo(USER1);
    GetMyInfoResponse response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameToken);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Double.valueOf(100.0), response.rating);
    assertEquals(USER1.username, response.username);

    jsonResponse = ServerConnection.getMyInfo(USER2);
    response = GsonSingleton.fromJson(jsonResponse, GetMyInfoResponse.class);
    assertEquals(true, response.result);
    assertNull(response.currentGameToken);
    assertEquals(false, response.isSearchingForGame);
    assertEquals(Double.valueOf(-100.0), response.rating);
    assertEquals(USER2.username, response.username);
  }

  private static class GameSocketClient extends MaloWProcess
  {
    private String gameToken;
    private NetworkChannel server;
    private String username;

    public GameSocketClient(TestUser user, String gameToken)
    {
      this.username = user.username;
      this.gameToken = gameToken;
      this.server = new NetworkChannel(Config.GAME_SOCKET_SERVER_IP, Config.GAME_SOCKET_SERVER_PORT);
      this.server.setNotifier(this);
      this.server.start();
      this.server.sendData(GsonSingleton.toJson(new JoinGameRequest(this.gameToken)));
    }

    @Override
    public void life()
    {
      NetworkPacket packet = (NetworkPacket) this.waitEvent();
      SocketResponse response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(SocketMethod.JOIN_GAME_REQUEST, response.method);

      this.server.sendData(GsonSingleton.toJson(new SocketMessage(SocketMethod.READY)));
      packet = (NetworkPacket) this.waitEvent();
      response = GsonSingleton.fromJson(packet.getMessage(), SocketResponse.class);
      assertEquals(true, response.result);
      assertEquals(SocketMethod.READY, response.method);

      packet = (NetworkPacket) this.waitEvent();
      GameMessage gameMessage = GsonSingleton.fromJson(packet.getMessage(), GameMessage.class);
      assertEquals(SocketMethod.GAME_MESSAGE, gameMessage.method);
      GameStateUpdate gameStateUpdate = (GameStateUpdate) gameMessage.getMessage();
      assertNotNull(gameStateUpdate);
      List<UnitData> units = gameStateUpdate.units;
      UnitData myUnit = units.stream().filter(u -> u.owner.equals(this.username)).findAny().get();

      List<Position> movePath = new ArrayList<>();
      movePath.add(new Position(1, 0));
      movePath.add(new Position(2, 1));
      movePath.add(new Position(2, 2));
      movePath.add(new Position(3, 2));
      movePath.add(new Position(4, 3));
      movePath.add(new Position(4, 4));
      movePath.add(new Position(4, 5));

      if (myUnit.position.equals(new Position(0, 0)))
      {
        this.server.sendData(GsonSingleton.toJson(new GameMessage(new MoveAction(myUnit.unitId, movePath))));
        this.server.sendData(GsonSingleton.toJson(new GameMessage(new AttackAction(myUnit.unitId, new Position(5, 5)))));
      }
      else
      {
        packet = (NetworkPacket) this.waitEvent();
        gameMessage = GsonSingleton.fromJson(packet.getMessage(), GameMessage.class);
        assertEquals(SocketMethod.GAME_MESSAGE, gameMessage.method);
        MoveAction moveAction = (MoveAction) gameMessage.getMessage();
        assertNotEquals(myUnit.unitId, moveAction.unitId);
        assertEquals(moveAction.path, movePath);

        packet = (NetworkPacket) this.waitEvent();
        gameMessage = GsonSingleton.fromJson(packet.getMessage(), GameMessage.class);
        assertEquals(SocketMethod.GAME_MESSAGE, gameMessage.method);
        AttackAction attackAction = (AttackAction) gameMessage.getMessage();
        assertNotEquals(myUnit.unitId, attackAction.unitId);
        assertEquals(attackAction.target, myUnit.position);
      }

      packet = (NetworkPacket) this.waitEvent();
      gameMessage = GsonSingleton.fromJson(packet.getMessage(), GameMessage.class);
      assertEquals(SocketMethod.GAME_MESSAGE, gameMessage.method);
      GameFinishedUpdate gameFinishedUpdate = (GameFinishedUpdate) gameMessage.getMessage();
      assertEquals(USER1.username, gameFinishedUpdate.winner);
    }

    @Override
    public void closeSpecific()
    {
    }
  }
}
