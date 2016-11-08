package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.List;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.malowlib.MaloWLogger;

public class MatchmakerHandler
{
  private static List<Player> playerQueue = new ArrayList<Player>();

  public static void queue(Player player)
  {
    synchronized (playerQueue)
    {
      if (playerQueue.contains(player))
      {
        MaloWLogger.error("Player tried to queue but was already in queue: " + player.username, new Exception());
        return;
      }

      Player other = findOpponent(player);
      if (other != null)
      {
        playerQueue.remove(other);
        GameInstanceHandler.createGame(player, other);
      }
      else
      {
        playerQueue.add(player);
      }
    }
  }

  public static void unqueue(Player player)
  {
    synchronized (playerQueue)
    {
      if (playerQueue.contains(player))
      {
        playerQueue.remove(player);
      }
    }
  }

  public static void clearQueue()
  {
    synchronized (playerQueue)
    {
      playerQueue.clear();
    }
  }

  private static Player findOpponent(Player player)
  {
    for (Player other : playerQueue)
    {
      if (Math.abs(other.rating - player.rating) <= GladiatorArenaServerConfig.MATCHMAKING_MAX_RATING_DIFFERENCE) { return other; }
    }
    return null;
  }
}
