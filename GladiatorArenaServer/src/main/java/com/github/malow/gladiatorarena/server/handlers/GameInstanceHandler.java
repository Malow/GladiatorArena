package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.database.AccountAccessor.WrongAuthentificationTokenException;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.MatchAccessor;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.database.PlayerAccessor;
import com.github.malow.gladiatorarena.server.game.GameInstance;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketErrorResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketJoinGameRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.MaloWProcess;
import com.github.malow.malowlib.ProcessEvent;
import com.github.malow.malowlib.network.NetworkChannel;

public class GameInstanceHandler extends MaloWProcess
{
  private static ConcurrentHashMap<Long, GameInstance> games = new ConcurrentHashMap<Long, GameInstance>();
  private static GameInstanceHandler INSTANCE;

  private List<Client> clients;

  private GameInstanceHandler()
  {
    this.clients = new ArrayList<Client>();
  }

  public static GameInstanceHandler getInstance()
  {
    if (INSTANCE == null) INSTANCE = new GameInstanceHandler();
    return INSTANCE;
  }

  public static void createGame(Player p1, Player p2)
  {
    try
    {
      Match match = new Match(null, p1, p2);
      match = MatchAccessor.create(match);
      p1.isSearchingForGame = false;
      p2.isSearchingForGame = false;
      p1.currentMatchId = match.id;
      p2.currentMatchId = match.id;
      PlayerAccessor.updateCacheOnly(p1);
      PlayerAccessor.updateCacheOnly(p2);

      GameInstance game = new GameInstance(p1, p2, match);
      game.start();
      games.put(match.id, game);
    }
    catch (Exception e)
    {
      MaloWLogger.error("Failed to create match", e);
      p1.isSearchingForGame = false;
      p2.isSearchingForGame = false;
      p1.currentMatchId = null;
      p2.currentMatchId = null;
      PlayerAccessor.updateCacheOnly(p1);
      PlayerAccessor.updateCacheOnly(p2);
    }
  }

  public static void deleteGame(Long matchId)
  {
    MaloWLogger.info("Deleting game: " + matchId);
    games.remove(matchId);
  }

  @Override
  public void life()
  {
    while (this.stayAlive)
    {
      ProcessEvent ev = this.waitEvent();
      if (ev instanceof GameNetworkPacket)
      {
        GameNetworkPacket packet = (GameNetworkPacket) ev;
        SocketJoinGameRequest req = GsonSingleton.get().fromJson(packet.message, SocketJoinGameRequest.class);
        if (req != null && req.isValid() && req.method.equals(GladiatorArenaServerConfig.JOIN_GAME_REQUEST_NAME))
        {
          this.handleRequest(req, packet.client);
        }
        else
        {
          packet.client.sendData(GsonSingleton.get().toJson(new SocketErrorResponse(req.method, false, "Unexpected method")));
        }
      }
    }
  }

  private void handleRequest(SocketJoinGameRequest req, Client client)
  {
    try
    {
      Long accId = AccountServer.checkAuthentication(req.email, req.authToken);
      client.accId = accId;
      client.email = req.email;
      client.authToken = req.authToken;
      GameInstance game = games.get(req.gameId);
      if (game != null && game.clientConnected(client))
      {
        client.setNotifier(game);
        this.clients.remove(client);
        client.sendData(GsonSingleton.get().toJson(new SocketResponse(req.method, true)));
      }
      else
      {
        client.sendData(GsonSingleton.get().toJson(new SocketResponse(req.method, false)));
      }
    }
    catch (WrongAuthentificationTokenException e)
    {
      client.sendData(GsonSingleton.get().toJson(new SocketResponse(req.method, false)));
    }
  }

  public void clientConnected(NetworkChannel nc)
  {
    if (nc instanceof Client)
    {
      Client client = (Client) nc;
      client.setNotifier(this);
      client.start();
      synchronized (this.clients)
      {
        this.clients.add(client);
      }
    }
    else
    {
      MaloWLogger.error("Client connected that was not of type Client", new Exception());
    }
  }

  @Override
  public void closeSpecific()
  {
    synchronized (this.clients)
    {
      for (NetworkChannel client : this.clients)
      {
        client.close();
      }
      for (NetworkChannel client : this.clients)
      {
        client.waitUntillDone();
      }
    }
  }
}
