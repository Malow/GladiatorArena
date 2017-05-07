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
    if (matchingUser.isPresent())
    {
      Optional<NetworkPlayer> matchingPlayer = this.players.stream().filter(p -> p.userId.equals(player.userId)).findFirst();
      if (matchingPlayer.isPresent())
      {
        matchingPlayer.get().client.setNotifier(null);
        matchingPlayer.get().client.close();
        this.players.remove(matchingPlayer.get());
      }
      this.players.add(player);
      this.game.addPlayer(player);
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
          if (this.isAllUsersConnected() && this.isAllUsersReady())
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
          if (this.isAllUsersConnected() && this.isAllUsersReady())
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
          if (this.isAllUsersConnected())
          {
            Optional<GameResult> gameResult = this.game.update(System.currentTimeMillis() - this.lastGameUpdate);
            this.lastGameUpdate = System.currentTimeMillis();
            if (gameResult.isPresent())
            {
              this.status = GameStatus.FINISHED;
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
            this.status = GameStatus.PAUSED_FOR_RECONNECT;
          }
          break;
        case FINISHED:
        case TIMED_OUT:
          if (this.players.stream().allMatch(c -> c.disconnected)
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
        from.client.sendData(GsonSingleton.toJson(new SocketResponse(message.method, true)));
        break;
      case GAME_MESSAGE:
        GameMessage gameMessage = GsonSingleton.fromJson(packet.message, GameMessage.class);
        boolean result = this.game.handleMessage(gameMessage.getMessage(), from.username);
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

  @Override
  public void closeSpecific()
  {
    this.players.stream().forEach(c -> c.client.close());
    this.players.stream().forEach(c -> c.client.waitUntillDone());
  }
}
