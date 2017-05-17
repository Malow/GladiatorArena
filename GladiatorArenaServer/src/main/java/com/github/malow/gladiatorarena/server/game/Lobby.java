package com.github.malow.gladiatorarena.server.game;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.malow.gladiatorarena.gamecore.Game;
import com.github.malow.gladiatorarena.gamecore.GameResult;
import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.database.User;
import com.github.malow.gladiatorarena.server.game.socketnetwork.GameNetworkPacket;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameStatusUpdate;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.LobbyInformationMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.ReadyMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.gladiatorarena.server.handlers.MatchHandlerSingleton;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.malowprocess.ProcessEvent;

public class Lobby extends MaloWProcess
{
  private int id;
  private List<User> users;
  private List<NetworkPlayer> players = new ArrayList<>();
  private LocalDateTime created;
  private LocalDateTime ended;
  private Game game;
  private GameStatus status = GameStatus.NOT_STARTED;
  private long lastGameUpdate = 0;
  //private long lastPing = 0;

  public Lobby(int id, List<User> expectedUsers)
  {
    this.id = id;
    this.users = expectedUsers;
    this.created = LocalDateTime.now();
    this.game = new Game();
  }

  public boolean userConnected(NetworkPlayer player)
  {
    Optional<User> matchingUser = this.users.stream().filter(p -> p.getId().equals(player.userId)).findFirst();
    if (!matchingUser.isPresent())
    {
      MaloWLogger.info("Unexpected Player connected to a lobby with username: " + player.username);
      return false;
    }

    User user = matchingUser.get();
    if (!user.currentGameToken.equals(player.gameToken))
    {
      MaloWLogger.info("Player " + player.username + " tried to connect to a lobby with a bad gameToken: " + player.gameToken + ". Expected: "
          + user.currentGameToken);
      return false;
    }

    Optional<NetworkPlayer> matchingPlayer = this.players.stream().filter(p -> p.userId.equals(player.userId)).findFirst();
    if (matchingPlayer.isPresent())
    {
      MaloWLogger.info(
          "Player " + player.username + " connected to a lobby to which he was already connected, and as such the previous connection was removed.");
      matchingPlayer.get().client.setNotifier(null);
      matchingPlayer.get().client.close();
      this.players.remove(matchingPlayer.get());
    }
    this.players.add(player);
    this.game.addPlayer(player);
    MaloWLogger.info("Player " + player.username + " connected added to lobby.");

    Map<String, Boolean> playersReady = new HashMap<>();
    this.players.stream().forEach(p -> playersReady.put(p.username, p.ready));
    this.sendToAllConnectedClients(new LobbyInformationMessage(playersReady));
    return true;
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
          if (this.isAllUsersConnected() && this.isAllUsersReady())
          {
            MaloWLogger.info("Game started.");
            this.updateStatus(GameStatus.IN_PROGRESS);
            this.game.startGame();
            this.lastGameUpdate = System.currentTimeMillis();
          }
          else if (this.isTimedOut(this.created, GladiatorArenaServerConfig.PRE_GAME_TIMEOUT_SECONDS))
          {
            // TODO: Handle dropping game due to not all clients connected.
          }
          this.sleep();
        case PAUSED_FOR_RECONNECT:
          if (this.isAllUsersConnected() && this.isAllUsersReady())
          {
            MaloWLogger.info("All players have reconnected to the game and it is resumed.");
            this.updateStatus(GameStatus.IN_PROGRESS);
            this.lastGameUpdate = System.currentTimeMillis();
          }
          else if (this.isTimedOut(this.created, GladiatorArenaServerConfig.RECONNECT_TIMEOUT_SECONDS))
          {
            // Handle dropping started game due to disconnect
          }
          this.sleep();
          break;
        case IN_PROGRESS:
          if (this.isAllUsersConnected())
          {
            Optional<GameResult> gameResult = this.game.update(System.currentTimeMillis() - this.lastGameUpdate);
            this.lastGameUpdate = System.currentTimeMillis();
            if (gameResult.isPresent())
            {
              this.updateStatus(GameStatus.FINISHED);
              this.ended = LocalDateTime.now();
              Map<User, Boolean> users = new HashMap<User, Boolean>();
              gameResult.get().winners.stream().map(w -> this.users.stream().filter(p -> p.username.equals(w.username)).findFirst().get())
                  .forEach(p -> users.put(p, true));
              gameResult.get().losers.stream().map(w -> this.users.stream().filter(p -> p.username.equals(w.username)).findFirst().get())
                  .forEach(p -> users.put(p, false));
              MatchResult matchResult = new MatchResult(users);
              MatchHandlerSingleton.get().handleEndedGame(this.id, matchResult);
            }
          }
          else
          {
            MaloWLogger.info("Players have dropped from an on-going game and it is now paused for reconnect.");
            this.updateStatus(GameStatus.PAUSED_FOR_RECONNECT);
          }
          break;
        case FINISHED:
        case TIMED_OUT:
          if (this.players.stream().allMatch(c -> c.disconnected)
              || this.isTimedOut(this.ended, GladiatorArenaServerConfig.POST_GAME_DURATION_SECONDS))
          {
            MaloWLogger.info("Game is timedout and closed.");
            this.close();
          }
          this.sleep();
          break;
        default:
          break;
      }

      // TODO: Make some sort of thread pool for all lobbies, and some sort of advanced sleep depending on how long execution time took (like MatchmakingEngine).
      try
      {
        Thread.sleep(10);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  private void updateStatus(GameStatus newStatus)
  {
    this.status = newStatus;
    this.sendToAllConnectedClients(new GameStatusUpdate(newStatus));
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

  private boolean isAllUsersReady()
  {
    return this.players.stream().allMatch(c -> c.ready);
  }

  private boolean isAllUsersConnected()
  {
    return this.players.size() == this.users.size();
  }

  private void handlePacket(GameNetworkPacket packet)
  {
    SocketMessage message = GsonSingleton.fromJson(packet.message, SocketMessage.class);
    NetworkPlayer from = this.getNetworkPlayerByUserId(packet.client.userId);
    switch (message.method)
    {
      case READY:
        from.ready = true;
        MaloWLogger.info("Player " + from.username + " is now ready.");
        this.sendToAllConnectedClients(new ReadyMessage(from.username));
        break;
      case GAME_MESSAGE:
        GameMessage gameMessage = GsonSingleton.fromJson(packet.message, GameMessage.class);
        boolean result = this.game.handleMessage(gameMessage.getMessage(), from);
        if (!result)
        {
          MaloWLogger.error("Returned false from game.handleMessage: " + gameMessage.method, new Exception());
        }
        break;
      default:
        MaloWLogger.error("Recieved a GameNetworkPacket with unkown method: " + message.method, new Exception());
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

  private NetworkPlayer getNetworkPlayerByUserId(Integer userId)
  {
    return this.players.stream().filter(p -> p.userId.equals(userId)).findFirst().get();
  }

  private void sendToAllConnectedClients(SocketMessage message)
  {
    this.players.stream().forEach(p -> p.client.sendData(GsonSingleton.toJson(message)));
  }

  @Override
  public void closeSpecific()
  {
    this.players.stream().forEach(c -> c.client.close());
    this.players.stream().forEach(c -> c.client.waitUntillDone());
  }
}
