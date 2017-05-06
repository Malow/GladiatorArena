package com.github.malow.gladiatorarena.server.game;

import java.util.Map;

import com.github.malow.gladiatorarena.server.database.User;

public class MatchResult
{
  public Map<User, Boolean> users;

  public MatchResult(Map<User, Boolean> players)
  {
    this.users = players;
  }
}
