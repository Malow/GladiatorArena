package com.github.malow.gladiatorarena.server.game;

import java.util.Map;

import com.github.malow.gladiatorarena.server.database.Player;

public class MatchResult
{
  public Map<Player, Boolean> players;

  public MatchResult(Map<Player, Boolean> players)
  {
    this.players = players;
  }
}
