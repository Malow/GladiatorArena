package com.github.malow.gladiatorarena.gamecore;

import java.time.LocalDateTime;
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
import com.github.malow.gladiatorarena.gamecore.message.GameStateUpdate;
import com.github.malow.gladiatorarena.gamecore.message.Message;
import com.github.malow.gladiatorarena.gamecore.message.MoveAction;
import com.github.malow.malowlib.MaloWLogger;

public class Game
{
  private List<Player> players = new ArrayList<>();
  private List<Unit> units = new ArrayList<>();
  private LocalDateTime roundStartedAt;
  private int nextUnitId = 0;
  private HexagonMap map = new HexagonMap(10, 10);

  public void addPlayer(Player player)
  {
    this.players.add(player);
    int unitId = this.nextUnitId++;
    Position position = new Position(unitId == 0 ? 0 : 5, unitId == 0 ? 0 : 5);
    Hexagon hexagon = this.map.get(position);
    Unit unit = new Unit(unitId, player.username, hexagon);
    hexagon.setUnit(unit);
    this.units.add(unit);
  }

  public void start()
  {
    this.nextTurn();
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
        this.endGame(winners.get(0));
        GameResult result = new GameResult(winners, losers);
        return Optional.of(result);
      }
    }

    if (this.hasAllPlayersFinishedTurn() || this.isTimedOut(this.roundStartedAt, GladiatorArenaGameConfig.GAME_ROUND_TIMEOUT_SECONDS))
    {
      this.nextTurn();
    }

    return Optional.empty();
  }

  private void nextTurn()
  {
    this.roundStartedAt = LocalDateTime.now();
    this.players.stream().forEach(p -> p.hasFinishedTurn = false);
    // Regen AP to mercs
    // Count down CDs to mercs abilities
    this.players.stream().forEach(p -> p.handleMessage(new GameStateUpdate(this.units)));
  }

  public boolean handleMessage(Message message, String fromUsername)
  {
    Player from = this.getPlayerByUsername(fromUsername);
    if (message instanceof MoveAction)
    {
      MoveAction action = (MoveAction) message;
      return this.handleMoveAction(action);
    }
    else if (message instanceof AttackAction)
    {
      AttackAction action = (AttackAction) message;
      return this.handleAttackAction(action);
    }
    else if (message instanceof FinishTurn)
    {
      from.hasFinishedTurn = true;
    }
    else
    {
      MaloWLogger.error("Got an unexpected request: " + message, new Exception());
    }
    return true;
  }

  private boolean handleMoveAction(MoveAction action)
  {
    Unit unit = this.units.stream().filter(u -> u.unitId == action.unitId).findAny().get();
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
      }
      else
      {
        boolean adjacent = HexagonHelper.isAdjacent(nextHexagon, unit.getPosition());
        boolean occupied = nextHexagon.isOccupied();
        MaloWLogger.error("Bad position, occupied: " + occupied + ", adjacent: " + adjacent + ". Current position: " + unit.getPosition().toString()
            + " - Next position: " + position.toString(), new Exception());
        currentHexagon.clearTile();
        originalHexagon.setUnit(unit);
        return false;
      }
    }
    this.players.stream().filter(p -> !p.username.equals(unit.owner)).forEach(p -> p.handleMessage(action));
    return true;
  }

  private boolean handleAttackAction(AttackAction action)
  {
    Unit unit = this.units.stream().filter(u -> u.unitId == action.unitId).findAny().get();
    Hexagon target = this.map.get(action.target);
    if (HexagonHelper.isAdjacent(target, unit.getPosition()) && target.isOccupied())
    {
      Unit victim = target.getUnit();
      victim.hitpoints -= 10;
    }
    else
    {
      return false;
    }
    this.players.stream().filter(p -> !p.username.equals(unit.owner)).forEach(p -> p.handleMessage(action));
    return true;
  }

  private void endGame(Player winner)
  {
    this.players.stream().forEach(p -> p.handleMessage(new GameFinishedUpdate(winner.username)));
  }

  private boolean isTimedOut(LocalDateTime from, int seconds)
  {
    return from.plusSeconds(seconds).isBefore(LocalDateTime.now());
  }

  private boolean hasAllPlayersFinishedTurn()
  {
    return this.players.stream().allMatch(p -> p.hasFinishedTurn);
  }

  private Player getPlayerByUsername(String username)
  {
    return this.players.stream().filter(p -> p.username.equals(username)).findFirst().get();
  }
}
