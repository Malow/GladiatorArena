package com.github.malow.gladiatorarena.server.gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import com.github.malow.gladiatorarena.gamecore.message.GameStateInformation;
import com.github.malow.gladiatorarena.gamecore.message.NextTurn;
import com.github.malow.gladiatorarena.gamecore.message.UnitData;
import com.github.malow.gladiatorarena.server.Config;
import com.github.malow.gladiatorarena.server.game.GameStatus;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameStatusUpdate;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.JoinGameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.LobbyInformationMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.ReadyMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.network.NetworkChannel;
import com.github.malow.malowlib.network.NetworkPacket;

public class TestGameClient extends MaloWProcess
{
  private String gameToken;
  private NetworkChannel server;
  private String myUsername;
  private String otherUsername;
  private boolean isFirst;

  public TestGameClient(String myUsername, String otherUsername, String gameToken, boolean isFirst)
  {
    this.myUsername = myUsername;
    this.otherUsername = otherUsername;
    this.gameToken = gameToken;
    this.isFirst = isFirst;
    this.server = new NetworkChannel(Config.GAME_SOCKET_SERVER_IP, Config.GAME_SOCKET_SERVER_PORT);
    this.server.setNotifier(this);
    this.server.start();
  }

  private <T> T waitForMessage(Class<? extends T> clazz)
  {
    NetworkPacket packet = (NetworkPacket) this.waitEvent();
    T response = GsonSingleton.fromJson(packet.getMessage(), clazz);
    return response;
  }

  private <T> T waitGameForMessage(Class<? extends T> clazz)
  {
    NetworkPacket packet = (NetworkPacket) this.waitEvent();
    GameMessage response = GsonSingleton.fromJson(packet.getMessage(), GameMessage.class);
    @SuppressWarnings("unchecked")
    T message = (T) response.getMessage();
    return message;
  }

  private void sendMessage(SocketMessage message)
  {
    this.server.sendData(GsonSingleton.toJson(message));
  }

  private void doLobby()
  {
    this.sendMessage(new JoinGameMessage(this.gameToken));

    LobbyInformationMessage lobbyInfoMessage = this.waitForMessage(LobbyInformationMessage.class);
    assertFalse(lobbyInfoMessage.players.get(this.myUsername));
    if (this.isFirst)
    {
      lobbyInfoMessage = this.waitForMessage(LobbyInformationMessage.class);
      assertEquals(lobbyInfoMessage.players.size(), 2);
      this.sendMessage(new ReadyMessage());
      ReadyMessage readyMessage = this.waitForMessage(ReadyMessage.class);
      assertEquals(readyMessage.username, this.myUsername);
      readyMessage = this.waitForMessage(ReadyMessage.class);
      assertEquals(readyMessage.username, this.otherUsername);
    }
    else
    {
      ReadyMessage readyMessage = this.waitForMessage(ReadyMessage.class);
      assertEquals(readyMessage.username, this.otherUsername);
      this.sendMessage(new ReadyMessage());
      readyMessage = this.waitForMessage(ReadyMessage.class);
      assertEquals(readyMessage.username, this.myUsername);
    }
    GameStatusUpdate gameStatusUpdate = this.waitForMessage(GameStatusUpdate.class);
    assertEquals(gameStatusUpdate.newStatus, GameStatus.IN_PROGRESS);
  }

  private void doGame()
  {
    GameStateInformation gameStateMessage = this.waitGameForMessage(GameStateInformation.class);
    List<UnitData> units = gameStateMessage.units;
    assertEquals(units.size(), 2);
    UnitData myUnit = units.stream().filter(u -> u.owner.equals(this.myUsername)).findAny().get();
    UnitData otherUnit = units.stream().filter(u -> u.owner.equals(this.otherUsername)).findAny().get();

    NextTurn nextTurnMessage = this.waitGameForMessage(NextTurn.class);
    if (this.isFirst)
    {
      assertEquals(nextTurnMessage.currentUnitId, myUnit.unitId);
    }
    else
    {
      assertEquals(nextTurnMessage.currentUnitId, otherUnit.unitId);
    }
  }

  @Override
  public void life()
  {
    this.doLobby();
    this.doGame();

    /*
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
    */
  }

  @Override
  public void closeSpecific()
  {
  }
}
