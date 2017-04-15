package com.github.malow.gladiatorarena.server.game;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.Globals;
import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketGameFinishedUpdateRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketGameStateUpdateRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.handlers.MatchHandler;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.database.DatabaseExceptions.MultipleRowsReturnedException;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;

public class GameInstance extends MaloWProcess
{
  private List<Player> players;
  private List<Client> clients;
  private Match match;

  private GameStatus status;

  private LocalDateTime roundStartedAt;
  private LocalDateTime endedAt;

  public GameInstance(Player p1, Player p2, Match match)
  {
    this.status = GameStatus.NOT_STARTED;
    this.players = new ArrayList<Player>();
    this.players.add(p1);
    this.players.add(p2);
    this.match = match;
    this.clients = new ArrayList<Client>();
  }

  public boolean clientConnected(Client client)
  {
    Optional<Player> matchingPlayer = this.players.stream().filter(p -> p.accountId.equals(client.accId)).findFirst();
    if (matchingPlayer.isPresent())
    {
      Optional<Client> matchingClient = this.clients.stream().filter(c -> c.accId.equals(client.accId)).findFirst();
      if (matchingClient.isPresent())
      {
        matchingClient.get().setNotifier(null);
        matchingClient.get().close();
        this.clients.remove(matchingClient.get());
      }
      this.clients.add(client);
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
      if (this.status == GameStatus.NOT_STARTED)
      {
        this.tryStartGame();
        if (this.status == GameStatus.NOT_STARTED)
        {
          if (this.checkTimedOut(this.match.createdAt, GladiatorArenaServerConfig.PRE_GAME_TIMEOUT_SECONDS))
          {
            this.status = GameStatus.TIMED_OUT;
            this.endGame(null);
          }
          else
          {
            try
            {
              Thread.sleep(GladiatorArenaServerConfig.SLEEP_DURATION_BETWEEN_LOBBY_UPDATES_MILLISECONDS);
            }
            catch (InterruptedException e)
            {
              MaloWLogger.error("Failed to Sleep", e);
            }
          }
        }
      }
      else if (this.status == GameStatus.IN_PROGRESS)
      {
        this.updateGame();
      }
      else
      {
        if (this.clients.stream().allMatch(c -> c.disconnected)
            || this.checkTimedOut(this.endedAt, GladiatorArenaServerConfig.POST_GAME_DURATION_SECONDS))
        {
          this.close();
        }
      }
    }
  }

  private boolean checkTimedOut(LocalDateTime from, int seconds)
  {
    LocalDateTime timeout = from.plusSeconds(seconds);
    if (timeout.isBefore(LocalDateTime.now()))
    {
      return true;
    }
    return false;
  }

  private void tryStartGame()
  {
    if (this.clients.size() == this.players.size() && this.clients.stream().allMatch(c -> c.ready))
    {
      // generate map etc.
      this.nextTurn();
      this.status = GameStatus.IN_PROGRESS;
    }
  }

  private void updateGame()
  {
    if (this.clients.stream().allMatch(c -> c.ready)
        || this.checkTimedOut(this.roundStartedAt, GladiatorArenaServerConfig.GAME_ROUND_TIMEOUT_SECONDS))
    {
      //nextTurn();
      this.status = GameStatus.FINISHED;
      this.endGame(this.players.get(0));
    }
  }

  private void nextTurn()
  {
    this.roundStartedAt = LocalDateTime.now();
    this.clients.stream().forEach(c -> c.ready = false);
    this.clients.stream().forEach(
        c -> c.sendData(GsonSingleton.toJson(new SocketGameStateUpdateRequest(GladiatorArenaServerConfig.GAME_STATE_UPDATE_REQUEST_NAME, "test"))));
    // Regen AP to mercs
    // Count down CDs to mercs abilities
  }

  private void handlePacket(GameNetworkPacket packet)
  {
    SocketRequest req = GsonSingleton.fromJson(packet.message, SocketRequest.class);
    switch (req.method)
    {
      case GladiatorArenaServerConfig.READY_REQUEST_NAME:
        packet.client.ready = true;
        packet.client.sendData(GsonSingleton.toJson(new SocketResponse(req.method, true)));
        break;
      case GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME:
        packet.client.disconnected = true;
        break;
      /*
      default:
      MaloWLogger.error("Unexpected msg received from client " + packet.client.getChannelID() + ": " + packet.message, new Exception());
      packet.client.sendData(GsonSingleton.toJson(new SocketErrorResponse(req.method, false, "Unexpected method")));
      */
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

  private void endGame(Player winner)
  {
    this.clients.stream().forEach(c -> c.sendData(
        GsonSingleton.toJson(new SocketGameFinishedUpdateRequest(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, winner.username))));
    if (winner != null)
    {
      this.calculateAndSetRatings(winner);
      this.match.winnerUsername = winner.username;
    }

    this.players.stream().forEach(p -> p.currentMatchId = null);
    this.players.stream().forEach(p ->
    {
      try
      {
        Globals.playerAccessor.update(p);
      }
      catch (UnexpectedException | ZeroRowsReturnedException | MultipleRowsReturnedException e)
      {
        MaloWLogger.error("Failed to update " + p.username, e);
      }
    });

    this.match.status = this.status;
    this.match.finishedAt = LocalDateTime.now();
    try
    {
      Globals.matchAccessor.update(this.match);
    }
    catch (UnexpectedException | ZeroRowsReturnedException | MultipleRowsReturnedException e)
    {
      MaloWLogger.error("Failed to update match " + this.match.getId(), e);
    }
    MatchHandler.getInstance().deleteEndedMatch(this.match.getId());
    this.endedAt = LocalDateTime.now();
  }

  private void calculateAndSetRatings(Player winner)
  {

    this.players.stream().forEach(p ->
    {
      if (winner.equals(p))
      {
        p.rating += 100;
        if (p.getId().equals(this.match.player1Id))
        {
          this.match.ratingChangePlayer1 = 100;
        }
        else
        {
          this.match.ratingChangePlayer1 = 100;
        }
      }
      else
      {
        p.rating -= 100;
        if (p.getId().equals(this.match.player2Id))
        {
          this.match.ratingChangePlayer2 = -100;
        }
        else
        {
          this.match.ratingChangePlayer2 = -100;
        }
      }
    });
  }

  @Override
  public void closeSpecific()
  {
    this.clients.stream().forEach(c -> c.close());
    this.clients.stream().forEach(c -> c.waitUntillDone());
  }
}
