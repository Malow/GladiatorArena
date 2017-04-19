package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.database.AccountAccessor.WrongAuthentificationTokenException;
import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.MatchAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.MatchReference;
import com.github.malow.gladiatorarena.server.database.MatchReferenceAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.database.PlayerAccessorSingleton;
import com.github.malow.gladiatorarena.server.game.Lobby;
import com.github.malow.gladiatorarena.server.game.MatchResult;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.MethodNames;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketErrorResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.specific.JoinGameRequest;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;
import com.github.malow.malowlib.network.NetworkChannel;

public class MatchHandler extends MaloWProcess
{
  private List<Client> clients = new ArrayList<Client>();
  private ConcurrentHashMap<Integer, Lobby> lobbies = new ConcurrentHashMap<Integer, Lobby>();
  private static int nextGameId = 0;

  protected MatchHandler()
  {
  }

  public void createNewGame(List<Player> expectedPlayers)
  {
    try
    {
      int gameId = nextGameId++;
      expectedPlayers.stream().forEach(p ->
      {
        p.isSearchingForGame = false;
        p.currentGameId = gameId;
        PlayerAccessorSingleton.get().updateCacheOnly(p);
      });
      Lobby lobby = new Lobby(gameId, expectedPlayers);
      lobby.start();
      this.lobbies.put(gameId, lobby);
    }
    catch (Exception e)
    {
      MaloWLogger.error("Failed to create game", e);
      expectedPlayers.stream().forEach(p ->
      {
        p.isSearchingForGame = false;
        p.currentGameId = null;
        PlayerAccessorSingleton.get().updateCacheOnly(p);
      });
    }
  }

  public void handleEndedGame(Integer gameId, MatchResult matchResult)
  {
    this.lobbies.remove(gameId);
    // Lock all players and update them
    //String[] mutexNames = matchResult.players.keySet().stream().map(w -> w.toString()).collect(Collectors.toList());
    //NamedMutexList mutexes = NamedMutexHandler.getAndLockMultipleLocksByNames(mutexNames);
    try
    {
      Match match = new Match();
      match = MatchAccessorSingleton.get().create(match);
      Integer matchId = match.getId();
      List<MatchReference> references = new ArrayList<>();
      for (Player p : matchResult.players.keySet())
      {
        Player player = PlayerAccessorSingleton.get().read(p.getId());
        MatchReference reference = new MatchReference();
        reference.playerId = player.getId();
        reference.matchId = matchId;
        reference.isWinner = matchResult.players.get(p);
        reference.ratingBefore = player.rating;
        reference.ratingChange = reference.isWinner ? 100 : -100;
        reference.username = player.username;
        references.add(reference);
        player.rating += reference.isWinner ? 100 : -100;
        player.currentGameId = null;
        PlayerAccessorSingleton.get().update(player);
        MatchReferenceAccessorSingleton.get().create(reference);
      }
    }
    catch (Exception e)
    {
      MaloWLogger.error("Unexpected error when handling ended game.", e);
    }
    finally
    {
      //mutexes.unlockAll();
    }
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
        JoinGameRequest req = GsonSingleton.fromJson(packet.message, JoinGameRequest.class);
        if (req != null && req.isValid())
        {
          if (req.method.equals(MethodNames.JOIN_GAME_REQUEST))
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

  private void handleRequest(JoinGameRequest req, Client client)
  {
    try
    {
      Integer accId = AccountServer.checkAuthentication(req.email, req.authToken);
      client.playerId = PlayerAccessorSingleton.get().readByAccountId(accId).getId();
      Lobby lobby = this.lobbies.get(req.gameId);
      if (lobby != null && lobby.playerConnected(client))
      {
        client.setNotifier(lobby);
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
    catch (Exception e)
    {
      MaloWLogger.error("Unexpected error when player " + req.email + " tried to join game " + req.gameId, e);
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
      this.clients.add(client);
    }
    else
    {
      MaloWLogger.error("Client connected that was not of type Client", new Exception());
    }
  }

  @Override
  public void closeSpecific()
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
