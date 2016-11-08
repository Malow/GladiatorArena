package com.github.malow.gladiatorarena.server.game;

import java.util.Calendar;

import com.github.malow.accountserver.database.Database.UnexpectedException;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.MatchAccessor;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.database.PlayerAccessor;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketGameFinishedUpdateRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketGameStateUpdateRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.handlers.GameInstanceHandler;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.MaloWProcess;
import com.github.malow.malowlib.ProcessEvent;

public class GameInstance extends MaloWProcess
{
  private ConnectedPlayer player1;
  private ConnectedPlayer player2;
  private Match match;

  public GameInstance(Player p1, Player p2, Match match)
  {
    this.player1 = new ConnectedPlayer(p1);
    this.player2 = new ConnectedPlayer(p2);
    this.match = match;
  }

  public boolean clientConnected(Client client)
  {
    if (client.accId.equals(this.player1.player.accountId))
    {
      if (this.player1.client != null)
      {
        this.player1.client.setNotifier(null);
        this.player1.client.close();
      }
      this.player1.client = client;
      return true;
    }
    else if (client.accId.equals(this.player2.player.accountId))
    {
      if (this.player2.client != null)
      {
        this.player2.client.setNotifier(null);
        this.player2.client.close();
      }
      this.player2.client = client;
      return true;
    }
    return false;
  }

  @Override
  public void life()
  {
    boolean gameStarted = false;
    long lastTime = System.nanoTime();
    long currentTime;
    long diff;
    while (this.stayAlive)
    {
      currentTime = System.nanoTime();
      diff = currentTime - lastTime;
      lastTime = currentTime;
      ProcessEvent ev = this.peekEvent();
      while (ev != null)
      {
        GameNetworkPacket packet = getGameNetworkPacket(ev);
        if (packet != null)
        {
          handlePacket(packet);
        }
        ev = this.peekEvent();
      }
      if (!gameStarted)
      {
        gameStarted = startGame();
        if (!gameStarted)
        {
          if (checkTimedOut(diff))
          {
            endGame(GameStatus.TIMED_OUT, null);
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
      else
      {
        updateGame(diff);
      }
    }
  }

  private boolean checkTimedOut(long diff)
  {
    Calendar timeout = match.createdAt;
    timeout.add(Calendar.SECOND, GladiatorArenaServerConfig.GAME_TIMEOUT_SECONDS);
    if (timeout.compareTo(Calendar.getInstance()) <= 0) return true;
    return false;
  }

  private boolean startGame()
  {
    if (this.player1.isReady() && this.player2.isReady())
    {
      // generate map etc.
      nextTurn();
      return true;
    }
    return false;
  }

  private void updateGame(long diff)
  {
    if (this.player1.isReady() && this.player2.isReady())
    {
      //nextTurn();
      endGame(GameStatus.FINISHED, this.player1);
    }
  }

  private void nextTurn()
  {
    player1.setReady(false);
    player2.setReady(false);
    this.player1.client
        .sendData(GsonSingleton.get().toJson(new SocketGameStateUpdateRequest(GladiatorArenaServerConfig.GAME_STATE_UPDATE_REQUEST_NAME, "test")));
    this.player2.client
        .sendData(GsonSingleton.get().toJson(new SocketGameStateUpdateRequest(GladiatorArenaServerConfig.GAME_STATE_UPDATE_REQUEST_NAME, "test")));
    // Regen AP to mercs
    // Count down CDs to mercs abilities
  }

  private void handlePacket(GameNetworkPacket packet)
  {
    SocketRequest req = GsonSingleton.get().fromJson(packet.message, SocketRequest.class);
    switch (req.method)
    {
      case GladiatorArenaServerConfig.READY_REQUEST_NAME:
        packet.client.ready = true;
        packet.client.sendData(GsonSingleton.get().toJson(new SocketResponse(req.method, true)));
        break;
      /*
      default:
      MaloWLogger.error("Unexpected msg received from client " + packet.client.getChannelID() + ": " + packet.message, new Exception());
      packet.client.sendData(GsonSingleton.get().toJson(new SocketErrorResponse(req.method, false, "Unexpected method")));
      */
    }
  }

  private GameNetworkPacket getGameNetworkPacket(ProcessEvent ev)
  {
    if (ev instanceof GameNetworkPacket) { return (GameNetworkPacket) ev; }
    MaloWLogger.error("Got a ProcessEvent that wasn't of type GameNetworkPacket", new Exception());
    return null;
  }

  private void endGame(GameStatus status, ConnectedPlayer winner)
  {
    if (winner != null)
    {
      this.calculateAndSetRatings(winner);
      this.player1.client.sendData(GsonSingleton.get()
          .toJson(new SocketGameFinishedUpdateRequest(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, winner.player.username)));
      this.player2.client.sendData(GsonSingleton.get()
          .toJson(new SocketGameFinishedUpdateRequest(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, winner.player.username)));
    }
    else
    {
      this.player1.client.sendData(
          GsonSingleton.get().toJson(new SocketGameFinishedUpdateRequest(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, null)));
      this.player2.client.sendData(
          GsonSingleton.get().toJson(new SocketGameFinishedUpdateRequest(GladiatorArenaServerConfig.GAME_FINISHED_UPDATE_REQUEST_NAME, null)));
    }

    this.player1.player.currentMatchId = null;
    this.player2.player.currentMatchId = null;

    try
    {
      PlayerAccessor.update(player1.player);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Failed to update " + player1.player.username, e);
    }
    try
    {
      PlayerAccessor.update(player2.player);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Failed to update " + player2.player.username, e);
    }

    this.match.winnerUsername = winner.player.username;
    this.match.status = status;
    this.match.finishedAt = Calendar.getInstance();
    try
    {
      MatchAccessor.update(this.match);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Failed to update match " + match.id, e);
    }
    this.close();
    GameInstanceHandler.deleteGame(this.match.id);
  }

  private void calculateAndSetRatings(ConnectedPlayer winner)
  {
    if (winner.equals(this.player1))
    {
      this.player1.player.rating += 100;
      this.match.ratingChangePlayer1 = 100;
      this.player2.player.rating -= 100;
      this.match.ratingChangePlayer2 = -100;
    }
    else
    {
      this.player1.player.rating -= 100;
      this.match.ratingChangePlayer1 = -100;
      this.player2.player.rating += 100;
      this.match.ratingChangePlayer2 = 100;
    }
  }

  @Override
  public void closeSpecific()
  {
    if (this.player1.client != null) this.player1.client.close();
    if (this.player2.client != null) this.player2.client.close();
    if (this.player1.client != null) this.player1.client.waitUntillDone();
    if (this.player2.client != null) this.player2.client.waitUntillDone();
  }
}
