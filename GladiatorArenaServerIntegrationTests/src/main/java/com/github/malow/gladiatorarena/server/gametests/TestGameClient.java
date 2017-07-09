package com.github.malow.gladiatorarena.server.gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;

import com.github.malow.gladiatorarena.gamecore.hex.Position;
import com.github.malow.gladiatorarena.gamecore.message.AttackAction;
import com.github.malow.gladiatorarena.gamecore.message.FinishTurn;
import com.github.malow.gladiatorarena.gamecore.message.GameFinishedUpdate;
import com.github.malow.gladiatorarena.gamecore.message.GameStateInformation;
import com.github.malow.gladiatorarena.gamecore.message.Message;
import com.github.malow.gladiatorarena.gamecore.message.MoveAction;
import com.github.malow.gladiatorarena.gamecore.message.NextTurn;
import com.github.malow.gladiatorarena.gamecore.message.UnitData;
import com.github.malow.gladiatorarena.server.Config;
import com.github.malow.gladiatorarena.server.game.GameStatus;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameStatusUpdate;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.JoinGameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.LobbyInformationMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.PlayerConnectedMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.ReadyMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.network.message.MessageNetworkChannel;
import com.github.malow.malowlib.network.message.NetworkMessage;

public class TestGameClient extends MaloWProcess
{
  private String gameToken;
  private MessageNetworkChannel server;
  private String myUsername;
  private String otherUsername;
  private boolean isFirst;

  public TestGameClient(String myUsername, String otherUsername, String gameToken, boolean isFirst)
  {
    this.myUsername = myUsername;
    this.otherUsername = otherUsername;
    this.gameToken = gameToken;
    this.isFirst = isFirst;
    this.server = new MessageNetworkChannel(Config.GAME_SOCKET_SERVER_IP, Config.GAME_SOCKET_SERVER_PORT);
    this.server.setNotifier(this);
  }

  private <T> T waitForMessage(Class<? extends T> clazz)
  {
    NetworkMessage packet = (NetworkMessage) this.waitEvent();
    T response = GsonSingleton.fromJson(packet.getMessage(), clazz);
    return response;
  }

  private <T> T waitGameForMessage(Class<? extends T> clazz)
  {
    NetworkMessage packet = (NetworkMessage) this.waitEvent();
    GameMessage response = GsonSingleton.fromJson(packet.getMessage(), GameMessage.class);
    @SuppressWarnings("unchecked")
    T message = (T) response.getMessage();
    return message;
  }

  private void sendMessage(SocketMessage message)
  {
    this.server.sendMessage(GsonSingleton.toJson(message));
  }

  private void sendGameMessage(Message message)
  {
    this.server.sendMessage(GsonSingleton.toJson(new GameMessage(message)));
  }

  private void doLobby()
  {
    this.sendMessage(new JoinGameMessage(this.gameToken));

    LobbyInformationMessage lobbyInfoMessage = this.waitForMessage(LobbyInformationMessage.class);
    assertEquals(lobbyInfoMessage.yourUsername, this.myUsername);
    assertFalse(lobbyInfoMessage.players.get(this.myUsername));

    if (this.isFirst)
    {
      assertEquals(lobbyInfoMessage.players.size(), 1);
      PlayerConnectedMessage playerConnectedMessage = this.waitForMessage(PlayerConnectedMessage.class);
      assertEquals(playerConnectedMessage.username, this.otherUsername);
      this.sendMessage(new ReadyMessage());
      ReadyMessage readyMessage = this.waitForMessage(ReadyMessage.class);
      assertEquals(readyMessage.username, this.myUsername);
      readyMessage = this.waitForMessage(ReadyMessage.class);
      assertEquals(readyMessage.username, this.otherUsername);
    }
    else
    {
      assertEquals(lobbyInfoMessage.players.size(), 2);
      assertFalse(lobbyInfoMessage.players.get(this.otherUsername));
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

    List<Position> movePath = Arrays.asList(new Position(1, 0), new Position(2, 1), new Position(2, 2), new Position(3, 2), new Position(4, 3),
        new Position(4, 4), new Position(4, 5));

    NextTurn nextTurn = this.waitGameForMessage(NextTurn.class);
    if (this.isFirst)
    {
      assertEquals(nextTurn.currentUnitId, myUnit.unitId);

      this.sendGameMessage(new MoveAction(myUnit.unitId, movePath));
      MoveAction moveAction = this.waitGameForMessage(MoveAction.class);
      assertEquals(moveAction.unitId, myUnit.unitId);
      Assertions.assertThat(moveAction.path).containsExactlyElementsOf(movePath);
      myUnit.position = moveAction.path.get(moveAction.path.size() - 1);

      this.sendGameMessage(new AttackAction(myUnit.unitId, otherUnit.position));
      AttackAction attackAction = this.waitGameForMessage(AttackAction.class);
      assertEquals(attackAction.unitId, myUnit.unitId);
      assertEquals(attackAction.target, otherUnit.position);

      this.sendGameMessage(new FinishTurn(myUnit.unitId));
      nextTurn = this.waitGameForMessage(NextTurn.class);
      assertEquals(nextTurn.currentUnitId, otherUnit.unitId);

      attackAction = this.waitGameForMessage(AttackAction.class);
      assertEquals(attackAction.unitId, otherUnit.unitId);
      assertEquals(attackAction.target, myUnit.position);

      nextTurn = this.waitGameForMessage(NextTurn.class);
      assertEquals(nextTurn.currentUnitId, myUnit.unitId);

      this.sendGameMessage(new AttackAction(myUnit.unitId, otherUnit.position));
      attackAction = this.waitGameForMessage(AttackAction.class);
      assertEquals(attackAction.unitId, myUnit.unitId);
      assertEquals(attackAction.target, otherUnit.position);
    }
    else
    {
      assertEquals(nextTurn.currentUnitId, otherUnit.unitId);

      MoveAction moveAction = this.waitGameForMessage(MoveAction.class);
      assertEquals(moveAction.unitId, otherUnit.unitId);
      Assertions.assertThat(moveAction.path).containsExactlyElementsOf(movePath);
      otherUnit.position = moveAction.path.get(moveAction.path.size() - 1);

      AttackAction attackAction = this.waitGameForMessage(AttackAction.class);
      assertEquals(attackAction.unitId, otherUnit.unitId);
      assertEquals(attackAction.target, myUnit.position);

      nextTurn = this.waitGameForMessage(NextTurn.class);
      assertEquals(nextTurn.currentUnitId, myUnit.unitId);

      this.sendGameMessage(new AttackAction(myUnit.unitId, otherUnit.position));
      attackAction = this.waitGameForMessage(AttackAction.class);
      assertEquals(attackAction.unitId, myUnit.unitId);
      assertEquals(attackAction.target, otherUnit.position);

      this.sendGameMessage(new FinishTurn(myUnit.unitId));
      nextTurn = this.waitGameForMessage(NextTurn.class);
      assertEquals(nextTurn.currentUnitId, otherUnit.unitId);

      attackAction = this.waitGameForMessage(AttackAction.class);
      assertEquals(attackAction.unitId, otherUnit.unitId);
      assertEquals(attackAction.target, myUnit.position);
    }
  }

  @Override
  public void life()
  {
    this.doLobby();
    this.doGame();

    GameFinishedUpdate gameFinished = this.waitGameForMessage(GameFinishedUpdate.class);
    assertEquals(gameFinished.winners.size(), 1);
    if (this.isFirst)
    {
      assertEquals(gameFinished.winners.get(0), this.myUsername);
    }
    else
    {
      assertEquals(gameFinished.winners.get(0), this.otherUsername);
    }
  }

  @Override
  public void closeSpecific()
  {
  }
}
