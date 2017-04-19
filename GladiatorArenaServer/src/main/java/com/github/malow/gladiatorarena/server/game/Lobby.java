package com.github.malow.gladiatorarena.server.game;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.handlers.MatchHandlerSingleton;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;

public class Lobby extends MaloWProcess
{
  private int id;
  private List<Player> players;
  private List<Client> clients = new ArrayList<>();
  private LocalDateTime created;
  private LocalDateTime ended;
  private GameInstance game;
  private GameStatus status = GameStatus.NOT_STARTED;
  private long lastGameUpdate = 0;
  //private long lastPing = 0;

  public Lobby(int id, List<Player> expectedPlayers)
  {
    this.id = id;
    this.players = expectedPlayers;
    this.created = LocalDateTime.now();
    this.game = new GameInstance();
  }

  public boolean playerConnected(Client client)
  {
    Optional<Player> matchingPlayer = this.players.stream().filter(p -> p.getId().equals(client.playerId)).findFirst();
    if (matchingPlayer.isPresent())
    {
      Optional<Client> matchingClient = this.clients.stream().filter(c -> c.playerId.equals(client.playerId)).findFirst();
      if (matchingClient.isPresent())
      {
        matchingClient.get().setNotifier(null);
        matchingClient.get().close();
        this.clients.remove(matchingClient.get());
      }
      client.username = matchingPlayer.get().username;
      this.clients.add(client);
      this.game.addClient(client);
      return true;
    }
    return false;
  }

  @Override
  public void life()
  {
    while (this.stayAlive)
    {
      ProcessEvent ev = this.peekEvent();
      while (ev != null)
      {
        GameNetworkPacket packet = this.getGameNetworkPacket(ev);
        if (packet != null)
        {
          this.handlePacket(packet);
        }
        ev = this.peekEvent();
      }

      switch (this.status)
      {
        case NOT_STARTED:
          if (this.isAllPlayersConnected() && this.isAllPlayersReady())
          {
            this.status = GameStatus.IN_PROGRESS;
            this.game.start();
            this.lastGameUpdate = System.currentTimeMillis();
          }
          else if (this.isTimedOut(this.created, GladiatorArenaServerConfig.PRE_GAME_TIMEOUT_SECONDS))
          {
            // Handle dropping game due to not all clients connected.
          }
          this.sleep();
        case PAUSED_FOR_RECONNECT:
          if (this.isAllPlayersConnected() && this.isAllPlayersReady())
          {
            this.status = GameStatus.IN_PROGRESS;
            this.lastGameUpdate = System.currentTimeMillis();
          }
          else if (this.isTimedOut(this.created, GladiatorArenaServerConfig.RECONNECT_TIMEOUT_SECONDS))
          {
            // Handle dropping started game due to dissconnect
          }
          this.sleep();
          break;
        case IN_PROGRESS:
          if (this.isAllPlayersConnected())
          {
            Optional<GameResult> gameResult = this.game.update(System.currentTimeMillis() - this.lastGameUpdate);
            this.lastGameUpdate = System.currentTimeMillis();
            if (gameResult.isPresent())
            {
              this.status = GameStatus.FINISHED;
              this.ended = LocalDateTime.now();
              Map<Player, Boolean> players = new HashMap<Player, Boolean>();
              gameResult.get().winners.stream().map(w -> this.players.stream().filter(p -> p.getId().equals(w.playerId)).findFirst().get())
                  .forEach(p -> players.put(p, true));
              gameResult.get().losers.stream().map(w -> this.players.stream().filter(p -> p.getId().equals(w.playerId)).findFirst().get())
                  .forEach(p -> players.put(p, false));
              MatchResult matchResult = new MatchResult(players);
              MatchHandlerSingleton.get().handleEndedGame(this.id, matchResult);
            }
          }
          else
          {
            this.status = GameStatus.PAUSED_FOR_RECONNECT;
          }
          break;
        case FINISHED:
        case TIMED_OUT:
          if (this.clients.stream().allMatch(c -> c.disconnected)
              || this.isTimedOut(this.ended, GladiatorArenaServerConfig.POST_GAME_DURATION_SECONDS))
          {
            this.close();
          }
          this.sleep();
          break;
        default:
          break;
      }
    }
  }

  private void sleep()
  {
    try
    {
      Thread.sleep(GladiatorArenaServerConfig.SLEEP_DURATION_IN_LOBBY);
    }
    catch (InterruptedException e)
    {
      MaloWLogger.error("Failed to Sleep", e);
    }
  }

  private boolean isTimedOut(LocalDateTime from, int seconds)
  {
    return from.plusSeconds(seconds).isBefore(LocalDateTime.now());
  }

  private boolean isAllPlayersReady()
  {
    return this.clients.stream().allMatch(c -> c.ready);
  }

  private boolean isAllPlayersConnected()
  {
    return this.clients.size() == this.players.size();
  }

  private void handlePacket(GameNetworkPacket packet)
  {
    SocketMessage message = GsonSingleton.fromJson(packet.message, SocketMessage.class);
    switch (message.method)
    {
      case MethodNames.READY:
        packet.client.ready = true;
        packet.client.sendData(GsonSingleton.toJson(new SocketResponse(message.method, true)));
        break;
      case MethodNames.GAME_FINISHED_UPDATE:
        packet.client.disconnected = true;
        break;
      case MethodNames.GAME_STATE_UPDATE:
        // Just acks, do nothing
        break;
      default:
        this.game.handleMessage(message, packet.client);
    }
  }

  private GameNetworkPacket getGameNetworkPacket(ProcessEvent ev)
  {
    if (ev instanceof GameNetworkPacket)
    {
      return (GameNetworkPacket) ev;
    }
    MaloWLogger.error("Got a ProcessEvent that wasn't of type GameNetworkPacket", new Exception());
    return null;
  }

  @Override
  public void closeSpecific()
  {
    this.clients.stream().forEach(c -> c.close());
    this.clients.stream().forEach(c -> c.waitUntillDone());
  }
}
