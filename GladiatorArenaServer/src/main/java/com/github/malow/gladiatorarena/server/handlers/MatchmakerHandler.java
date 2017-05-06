package com.github.malow.gladiatorarena.server.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.malow.gladiatorarena.server.GladiatorArenaServerConfig;
import com.github.malow.gladiatorarena.server.database.User;
import com.github.malow.malowlib.MaloWLogger;

public class MatchmakerHandler
{
  private static List<User> userQueue = new ArrayList<User>();

  public static void queue(User user)
  {
    synchronized (userQueue)
    {
      if (userQueue.contains(user))
      {
        MaloWLogger.error("User tried to queue but was already in queue: " + user.username, new Exception());
        return;
      }

      User other = findOpponent(user);
      if (other != null)
      {
        userQueue.remove(other);
        MatchHandlerSingleton.get().createNewGame(Arrays.asList(user, other));
      }
      else
      {
        userQueue.add(user);
      }
    }
  }

  public static void unqueue(User user)
  {
    synchronized (userQueue)
    {
      if (userQueue.contains(user))
      {
        userQueue.remove(user);
      }
    }
  }

  public static void clearQueue()
  {
    synchronized (userQueue)
    {
      userQueue.clear();
    }
  }

  private static User findOpponent(User user)
  {
    for (User other : userQueue)
    {
      if (Math.abs(other.rating - user.rating) <= GladiatorArenaServerConfig.MATCHMAKING_MAX_RATING_DIFFERENCE)
      {
        return other;
      }
    }
    return null;
  }
}
