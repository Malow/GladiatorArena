package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.database.AccountAccessor.WrongAuthentificationTokenException;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.Globals;
import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.game.GameInstance;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketErrorResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketJoinGameRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;
import com.github.malow.malowlib.network.NetworkChannel;

public class MatchHandler extends MaloWProcess
{
  private static MatchHandler INSTANCE;

  private List<Client> clients = new ArrayList<Client>();
  private ConcurrentHashMap<Integer, GameInstance> games = new ConcurrentHashMap<Integer, GameInstance>();

  private MatchHandler()
  {
  }

  public static MatchHandler getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new MatchHandler();
    }
    return INSTANCE;
  }

  public void createNewMatch(Player p1, Player p2)
  {
    try
    {
      Match match = new Match(p1, p2);
      match = Globals.matchAccessor.create(match);
      p1.isSearchingForGame = false;
      p2.isSearchingForGame = false;
      p1.currentMatchId = match.getId();
      p2.currentMatchId = match.getId();
      Globals.playerAccessor.updateCacheOnly(p1);
      Globals.playerAccessor.updateCacheOnly(p2);

      GameInstance game = new GameInstance(p1, p2, match);
      game.start();
      this.games.put(match.getId(), game);
    }
    catch (Exception e)
    {
      MaloWLogger.error("Failed to create match", e);
      p1.isSearchingForGame = false;
      p2.isSearchingForGame = false;
      p1.currentMatchId = null;
      p2.currentMatchId = null;
      Globals.playerAccessor.updateCacheOnly(p1);
      Globals.playerAccessor.updateCacheOnly(p2);
    }
  }

  public void deleteEndedMatch(Integer matchId)
  {
    this.games.remove(matchId);
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
        SocketJoinGameRequest req = GsonSingleton.fromJson(packet.message, SocketJoinGameRequest.class);
        if (req != null && req.isValid())
        {
          if (req.method.equals(GladiatorArenaServerConfig.JOIN_GAME_REQUEST_NAME))
          {
            this.handleRequest(req, packet.client);
          }
          else
          {
            packet.client.sendData(GsonSingleton.toJson(new SocketErrorResponse(req.method, false, "Unexpected method")));
          }
        }
        else
        {
          packet.client.sendData(GsonSingleton.toJson(new SocketErrorResponse("", false, "Bad Request")));
        }
      }
    }
  }

  private void handleRequest(SocketJoinGameRequest req, Client client)
  {
    try
    {
      Integer accId = AccountServer.checkAuthentication(req.email, req.authToken);
      client.accId = accId;
      GameInstance game = this.games.get(req.gameId);
      if (game != null && game.clientConnected(client))
      {
        client.setNotifier(game);
        this.clients.remove(client);
        client.sendData(GsonSingleton.toJson(new SocketResponse(req.method, true)));
      }
      else
      {
        client.sendData(GsonSingleton.toJson(new SocketResponse(req.method, false)));
      }
    }
    catch (WrongAuthentificationTokenException e)
    {
      client.sendData(GsonSingleton.toJson(new SocketResponse(req.method, false)));
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
