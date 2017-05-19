package com.github.malow.gladiatorarena.gamecore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.malow.gladiatorarena.gamecore.hex.Hexagon;
import com.github.malow.gladiatorarena.gamecore.hex.HexagonHelper;
import com.github.malow.gladiatorarena.gamecore.hex.HexagonMap;
import com.github.malow.gladiatorarena.gamecore.hex.Position;
import com.github.malow.gladiatorarena.gamecore.hex.Unit;
import com.github.malow.gladiatorarena.gamecore.message.AttackAction;
import com.github.malow.gladiatorarena.gamecore.message.FinishTurn;
import com.github.malow.gladiatorarena.gamecore.message.GameFinishedUpdate;
import com.github.malow.gladiatorarena.gamecore.message.GameStateInformation;
import com.github.malow.gladiatorarena.gamecore.message.Message;
import com.github.malow.gladiatorarena.gamecore.message.MoveAction;
import com.github.malow.gladiatorarena.gamecore.message.NextTurn;
import com.github.malow.malowlib.MaloWLogger;

public class Game
{
  private List<Player> players = new ArrayList<>();
  private List<Unit> units = new ArrayList<>();
  private UnitOrder unitOrder = new UnitOrder();
  private HexagonMap map = new HexagonMap(10, 10);

  public void addPlayer(Player player)
  {
    Position position = new Position(this.players.size() == 0 ? 0 : 5, this.players.size() == 0 ? 0 : 5);
    this.players.add(player);
    Hexagon hexagon = this.map.get(position);
    Unit unit = new Unit(player.username, hexagon);
    hexagon.setUnit(unit);
    this.units.add(unit);
    this.unitOrder.add(unit);
    MaloWLogger.info("Player " + player.username + " added to game, with unit " + unit.getId());
  }

  public void startGame()
  {
    this.unitOrder.resetTurnTimer();
    this.sendMessageToAll(new GameStateInformation(this.units));
    this.sendMessageToAll(new NextTurn(this.unitOrder.getCurrent().getId()));
  }

  private void nextTurn()
  {
    this.unitOrder.rotate();
    this.sendMessageToAll(new NextTurn(this.unitOrder.getCurrent().getId()));
  }

  public Optional<GameResult> update(long diff)
  {
    for (Player player : this.players)
    {
      boolean hasAliveUnits = this.units.stream().filter(u -> u.owner.equals(player.username)).anyMatch(u -> u.isAlive());
      if (!hasAliveUnits)
      {
        List<Player> losers = Arrays.asList(player);
        List<Player> winners = this.players.stream().filter(p -> !p.username.equals(player.username)).collect(Collectors.toList());
        this.sendMessageToAll(new GameFinishedUpdate(winners.stream().map(p -> p.username).collect(Collectors.toList())));
        GameResult result = new GameResult(winners, losers);
        MaloWLogger.info("Game finished, winner: " + winners.get(0).username);
        return Optional.of(result);
      }
    }

    if (this.unitOrder.hasTurnTimedOut())
    {
      Unit unit = this.unitOrder.getCurrent();
      MaloWLogger.info("Unit " + unit.getId() + " belonging to player " + unit.owner + " had its turn timed out in game");
      this.nextTurn();
    }

    return Optional.empty();
  }

  public boolean handleMessage(Message message, Player from)
  {
    if (message instanceof MoveAction)
    {
      MoveAction action = (MoveAction) message;
      if (!this.isUnitsTurnAndBelongsTo(action.unitId, from))
      {
        MaloWLogger.info(from.username + " tried to move with unit " + action.unitId + " but that unit either doesn't belong to " + from.username
            + " or it's not that unit's turn right now.");
        return false;
      }
      return this.handleMoveAction(this.unitOrder.getCurrent(), action);
    }
    else if (message instanceof AttackAction)
    {
      AttackAction action = (AttackAction) message;
      if (!this.isUnitsTurnAndBelongsTo(action.unitId, from))
      {
        MaloWLogger.info(from.username + " tried to attack with unit " + action.unitId + " but that unit either doesn't belong to " + from.username
            + " or it's not that unit's turn right now.");
        return false;
      }
      return this.handleAttackAction(this.unitOrder.getCurrent(), action);
    }
    else if (message instanceof FinishTurn)
    {
      FinishTurn finishTurn = (FinishTurn) message;
      if (!this.isUnitsTurnAndBelongsTo(finishTurn.unitId, from))
      {
        MaloWLogger.info(from.username + " tried to finish turn for unit " + finishTurn.unitId + " but that unit either doesn't belong to "
            + from.username + " or it's not that unit's turn right now.");
        return false;
      }
      this.handleFinishTurn(from, finishTurn);
    }
    else
    {
      MaloWLogger.error("Received an unexpected request: " + message, new Exception());
    }
    return true;
  }

  private boolean handleMoveAction(Unit unit, MoveAction action)
  {
    MaloWLogger.info("Unit " + unit.getId() + " initiating a move-action.");
    List<Position> path = action.path;
    Hexagon originalHexagon = this.map.get(unit.getPosition());
    for (Position position : path)
    {
      Hexagon currentHexagon = this.map.get(unit.getPosition());
      Hexagon nextHexagon = this.map.get(position);
      if (HexagonHelper.isAdjacent(nextHexagon, currentHexagon) && !nextHexagon.isOccupied())
      {
        currentHexagon.clearTile();
        nextHexagon.setUnit(unit);
        MaloWLogger.info("Unit " + unit.getId() + " moved to position " + nextHexagon.toString());
      }
      else
      {
        boolean adjacent = HexagonHelper.isAdjacent(nextHexagon, unit.getPosition());
        boolean occupied = nextHexagon.isOccupied();
        MaloWLogger.error("Bad position, occupied: " + occupied + ", adjacent: " + adjacent + ". Current position: " + unit.getPosition().toString()
            + " - Next position: " + position.toString(), new Exception());
        currentHexagon.clearTile();
        originalHexagon.setUnit(unit);
        MaloWLogger.info("Unit " + unit.getId() + " moved to position " + originalHexagon.toString());
        return false;
      }
    }
    this.sendMessageToAll(action);
    return true;
  }

  private boolean handleAttackAction(Unit unit, AttackAction action)
  {
    Hexagon target = this.map.get(action.target);
    if (HexagonHelper.isAdjacent(target, unit.getPosition()) && target.isOccupied())
    {
      Unit victim = target.getUnit();
      victim.hitpoints -= 5;
      MaloWLogger.info("Unit " + unit.getId() + " attacked unit " + victim.getId());
    }
    else
    {
      MaloWLogger.info("Unit " + unit.getId() + " at position " + unit.getPosition().toString() + " failed to attack position " + target.toString()
          + ". IsAdjacent: " + HexagonHelper.isAdjacent(target, unit.getPosition()) + ", IsOccupied: " + target.isOccupied());
      return false;
    }
    this.sendMessageToAll(action);
    return true;
  }

  private void sendMessageToAll(Message message)
  {
    this.players.stream().forEach(p -> p.handleMessage(message));
  }

  private boolean handleFinishTurn(Player from, FinishTurn finishTurn)
  {
    MaloWLogger.info(from.username + " finished turn for unit " + finishTurn.unitId + " in game");
    this.nextTurn();
    return true;
  }

  private boolean isUnitsTurnAndBelongsTo(int unitId, Player player)
  {
    Unit unit = this.unitOrder.getCurrent();
    if (unit.getId() != unitId)
    {
      return false;
    }
    if (!player.username.equals(unit.owner))
    {
      return false;
    }
    return true;
  }
}
