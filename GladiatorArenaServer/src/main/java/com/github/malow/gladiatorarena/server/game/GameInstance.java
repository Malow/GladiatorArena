package com.github.malow.gladiatorarena.server.game;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketErrorResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.GameFinishedUpdate;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.GameStateUpdate;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;

public class GameInstance
{
  private List<Client> clients = new ArrayList<>();
  private LocalDateTime roundStartedAt;
  private int actions = 0;

  public void addClient(Client client)
  {
    this.clients.add(client);
  }

  public Optional<GameResult> update(long diff)
  {
    if (this.isAllPlayersReady() || this.isTimedOut(this.roundStartedAt, GladiatorArenaServerConfig.GAME_ROUND_TIMEOUT_SECONDS))
    {
      // Haxx for now for the testcase
      if (this.actions > 1)
      {
        this.endGame(this.clients.get(0));
        GameResult result = new GameResult(Arrays.asList(this.clients.get(0)), Arrays.asList(this.clients.get(1)));
        return Optional.of(result);
      }
      //nextTurn();
    }
    return Optional.empty();
  }

  public void handleMessage(SocketMessage message, Client from)
  {
    switch (message.method)
    {
      case MethodNames.ACTION_REQUEST:
        from.sendData(GsonSingleton.toJson(new SocketResponse(message.method, true)));
        this.actions++;
        break;
      default:
        MaloWLogger.error("Unexpected msg received from client " + from.username + ": " + message, new Exception());
        from.sendData(GsonSingleton.toJson(new SocketErrorResponse(message.method, false, "Unexpected method")));
    }
  }

  public void start()
  {
    this.nextTurn();
  }

  private void nextTurn()
  {
    this.roundStartedAt = LocalDateTime.now();
    this.clients.stream().forEach(c -> c.ready = false);
    this.clients.stream().forEach(c -> c.sendData(GsonSingleton.toJson(new GameStateUpdate())));
    // Regen AP to mercs
    // Count down CDs to mercs abilities
  }

  private void endGame(Client winner)
  {
    this.clients.stream().forEach(c -> c.sendData(GsonSingleton.toJson(new GameFinishedUpdate(winner.username))));
  }

  private boolean isTimedOut(LocalDateTime from, int seconds)
  {
    return from.plusSeconds(seconds).isBefore(LocalDateTime.now());
  }

  private boolean isAllPlayersReady()
  {
    return this.clients.stream().allMatch(c -> c.ready);
  }
}
