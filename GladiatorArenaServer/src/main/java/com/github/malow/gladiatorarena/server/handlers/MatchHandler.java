package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.database.AccountAccessor.WrongAuthentificationTokenException;
import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.MatchAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.MatchReference;
import com.github.malow.gladiatorarena.server.database.MatchReferenceAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.User;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.gladiatorarena.server.game.Lobby;
import com.github.malow.gladiatorarena.server.game.MatchResult;
import com.github.malow.gladiatorarena.server.game.NetworkPlayer;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.JoinGameRequest;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketErrorResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage.SocketMethod;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketResponse;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;
import com.github.malow.malowlib.matchmakingengine.MatchFoundEvent;
import com.github.malow.malowlib.matchmakingengine.MatchmakingResult;
import com.github.malow.malowlib.network.NetworkChannel;

public class MatchHandler extends MaloWProcess
{
  private List<NetworkChannel> players = new ArrayList<NetworkChannel>();
  private ConcurrentHashMap<Integer, Lobby> lobbies = new ConcurrentHashMap<Integer, Lobby>();
  private static int nextGameId = 0;

  protected MatchHandler()
  {
  }

  private void createNewGame(List<User> expectedUsers)
  {
    try
    {
      int gameId = nextGameId++;
      expectedUsers.stream().forEach(p ->
      {
        p.isSearchingForGame = false;
        p.currentGameId = gameId;
        UserAccessorSingleton.get().updateCacheOnly(p);
      });
      Lobby lobby = new Lobby(gameId, expectedUsers);
      lobby.start();
      this.lobbies.put(gameId, lobby);
    }
    catch (Exception e)
    {
      MaloWLogger.error("Failed to create game", e);
      expectedUsers.stream().forEach(p ->
      {
        p.isSearchingForGame = false;
        p.currentGameId = null;
        UserAccessorSingleton.get().updateCacheOnly(p);
      });
    }
  }

  public void handleEndedGame(Integer gameId, MatchResult matchResult)
  {
    this.lobbies.remove(gameId);
    // Lock all users and update them
    //String[] mutexNames = matchResult.users.keySet().stream().map(w -> w.toString()).collect(Collectors.toList());
    //NamedMutexList mutexes = NamedMutexHandler.getAndLockMultipleLocksByNames(mutexNames);
    try
    {
      Match match = new Match();
      match = MatchAccessorSingleton.get().create(match);
      Integer matchId = match.getId();
      List<MatchReference> references = new ArrayList<>();
      for (User u : matchResult.users.keySet())
      {
        User user = UserAccessorSingleton.get().read(u.getId());
        MatchReference reference = new MatchReference();
        reference.userId = user.getId();
        reference.matchId = matchId;
        reference.isWinner = matchResult.users.get(u);
        reference.ratingBefore = user.rating;
        reference.ratingChange = reference.isWinner ? 100.0 : -100.0;
        reference.username = user.username;
        references.add(reference);
        user.rating += reference.isWinner ? 100.0 : -100.0;
        user.currentGameId = null;
        UserAccessorSingleton.get().update(user);
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
      if (ev instanceof MatchFoundEvent)
      {
        MatchFoundEvent event = (MatchFoundEvent) ev;
        MatchmakingResult matchMakingResult = event.matchmakingResult;
        try
        {
          User user1 = UserAccessorSingleton.get().read(matchMakingResult.player1.playerId);
          User user2 = UserAccessorSingleton.get().read(matchMakingResult.player2.playerId);
          this.createNewGame(Arrays.asList(user1, user2));
        }
        catch (Exception e)
        {
          MaloWLogger.error("Critical error, unable to find User for match", e);
        }
      }
      else if (ev instanceof GameNetworkPacket)
      {
        GameNetworkPacket packet = (GameNetworkPacket) ev;
        JoinGameRequest req = GsonSingleton.fromJson(packet.message, JoinGameRequest.class);
        if (req != null && req.isValid())
        {
          if (req.method.equals(SocketMethod.JOIN_GAME_REQUEST))
          {
            this.handleJoinGameRequest(req, packet.client);
          }
          else
          {
            packet.client.sendData(GsonSingleton.toJson(new SocketErrorResponse(req.method, false, "Unexpected method")));
          }
        }
        else
        {
          packet.client.sendData(GsonSingleton.toJson(new SocketErrorResponse(SocketMethod.UNKNOWN, false, "Bad Request")));
        }
      }
    }
  }

  private void handleJoinGameRequest(JoinGameRequest req, Client client)
  {
    try
    {
      Integer accId = AccountServer.authenticateAndGetAccountId(req.email, req.authToken);
      User user = UserAccessorSingleton.get().readByAccountId(accId);
      client.userId = user.getId();
      Lobby lobby = this.lobbies.get(req.gameId);
      if (lobby != null && lobby.userConnected(new NetworkPlayer(user, client)))
      {
        client.setNotifier(lobby);
        this.players.remove(client);
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
      MaloWLogger.error("Unexpected error when user " + req.email + " tried to join game " + req.gameId, e);
      client.sendData(GsonSingleton.toJson(new SocketResponse(req.method, false)));
    }
  }

  public void playerConnected(NetworkChannel nc)
  {
    if (nc instanceof Client)
    {
      Client client = (Client) nc;
      client.setNotifier(this);
      client.start();
      this.players.add(client);
      MaloWLogger.info("Client connected to MatchHandler");
    }
    else
    {
      MaloWLogger.error("Player connected that was not of type Client", new Exception());
    }
  }

  @Override
  public void closeSpecific()
  {
    for (NetworkChannel player : this.players)
    {
      player.close();
    }
    for (NetworkChannel player : this.players)
    {
      player.waitUntillDone();
    }
  }
}
