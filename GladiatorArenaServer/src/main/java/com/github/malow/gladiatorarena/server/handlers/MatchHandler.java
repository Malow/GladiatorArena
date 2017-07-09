package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import com.github.malow.gladiatorarena.server.game.socketnetwork.SocketErrorMessages;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.JoinGameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketErrorResponse;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage.SocketMethod;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;
import com.github.malow.malowlib.matchmakingengine.MatchFoundEvent;
import com.github.malow.malowlib.matchmakingengine.MatchmakingResult;
import com.github.malow.malowlib.network.ClientConnectedEvent;
import com.github.malow.malowlib.network.NetworkChannel;

public class MatchHandler extends MaloWProcess
{
  private List<NetworkChannel> players = new ArrayList<NetworkChannel>();
  private ConcurrentHashMap<Integer, Lobby> lobbies = new ConcurrentHashMap<Integer, Lobby>();
  private static int nextGameId = 0;

  protected MatchHandler()
  {
  }

  private void createNewGame(MatchmakingResult matchmakingResult)
  {
    //NamedMutexList mutexes = NamedMutexHandler.getAndLockMultipleLocksByNames(mutexNames);
    List<User> expectedUsers = this.getExpectedUsersFromMatchmakingResult(matchmakingResult);
    try
    {
      if (expectedUsers == null)
      {
        return;
      }
      int gameId = nextGameId++;
      expectedUsers.stream().forEach(p ->
      {
        String gameToken = gameId + "|" + UUID.randomUUID();
        p.isSearchingForGame = false;
        p.currentGameToken = gameToken;
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
        p.currentGameToken = null;
        UserAccessorSingleton.get().updateCacheOnly(p);
      });
    }
    finally
    {
      //mutexes.unlockAll();
    }
  }

  private List<User> getExpectedUsersFromMatchmakingResult(MatchmakingResult matchmakingResult)
  {
    try
    {
      User user1 = UserAccessorSingleton.get().read(matchmakingResult.player1.playerId);
      User user2 = UserAccessorSingleton.get().read(matchmakingResult.player2.playerId);
      List<User> expectedUsers = Arrays.asList(user1, user2);
      return expectedUsers;
    }
    catch (Exception e)
    {
      MaloWLogger.error("Failed to get users for createNewGame", e);
      return null;
    }
  }

  public void handleEndedGame(Integer gameId, MatchResult matchResult)
  {
    this.lobbies.remove(gameId);
    // TODO: Lock all users and update them
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
        user.currentGameToken = null;
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
        MatchmakingResult matchmakingResult = event.matchmakingResult;
        try
        {
          this.createNewGame(matchmakingResult);
        }
        catch (Exception e)
        {
          MaloWLogger.error("Critical error, unable to find User for match", e);
        }
      }
      else if (ev instanceof ClientConnectedEvent)
      {
        ClientConnectedEvent cce = (ClientConnectedEvent) ev;
        this.handlePlayerConnected(cce.client);
      }
      else if (ev instanceof GameNetworkPacket)
      {
        GameNetworkPacket packet = (GameNetworkPacket) ev;
        JoinGameMessage req = GsonSingleton.fromJson(packet.message, JoinGameMessage.class);
        if (req != null && req.isValid())
        {
          if (req.method.equals(SocketMethod.JOIN_GAME))
          {
            this.handleJoinGameRequest(req, packet.client);
          }
          else
          {
            MaloWLogger.info("MatchHandler got an unexpected method: " + req.method);
            packet.client.sendMessage(GsonSingleton.toJson(new SocketErrorResponse(req.method, "Unexpected method")));
          }
        }
        else
        {
          MaloWLogger.info("MatchHandler got a bad request: " + packet.message);
          packet.client.sendMessage(GsonSingleton.toJson(new SocketErrorResponse(SocketMethod.UNKNOWN, "Bad Request")));
        }
      }
    }
  }

  private void handleJoinGameRequest(JoinGameMessage req, Client client)
  {
    try
    {
      String gameToken = req.gameToken;
      User user = UserAccessorSingleton.get().readByGameToken(gameToken);
      client.userId = user.getId();
      int matchId = Integer.parseInt(gameToken.split("|")[0]);
      Lobby lobby = this.lobbies.get(matchId);
      if (lobby != null && lobby.userConnected(new NetworkPlayer(user, client, gameToken)))
      {
        client.setNotifier(lobby);
        this.players.remove(client);
      }
      else
      {
        client.sendMessage(GsonSingleton.toJson(new SocketErrorResponse(req.method, SocketErrorMessages.FAILED_TO_JOIN_GAME)));
      }
    }
    catch (Exception e)
    {
      MaloWLogger.error("Unexpected error when user with gameToken " + req.gameToken + " tried to join lobby", e);
      client.sendMessage(GsonSingleton.toJson(new SocketErrorResponse(req.method, SocketErrorMessages.FAILED_TO_JOIN_GAME)));
    }
  }

  private void handlePlayerConnected(NetworkChannel nc)
  {
    if (nc instanceof Client)
    {
      Client client = (Client) nc;
      client.setNotifier(this);
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
