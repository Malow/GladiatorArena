package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.List;

import com.github.malow.gladiatorarena.server.database.Player;

public class MatchmakerHandler
{
  private static List<Player> playerQueue = new ArrayList<Player>();

  public static void Queue(Player player)
  {
    synchronized (playerQueue)
    {
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

  public static void DeQueue(Player player)
  {
    synchronized (playerQueue)
    {
      if (playerQueue.contains(player))
      {
        playerQueue.remove(player);
      }
    }
  }

  private static Player findOpponent(Player player)
  {
    for (Player other : playerQueue)
    {
      if (Math.abs(other.rating - player.rating) < 100) { return other; }
    }
    return null;
  }
}
