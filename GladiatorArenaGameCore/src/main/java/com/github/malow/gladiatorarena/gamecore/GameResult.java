package com.github.malow.gladiatorarena.gamecore;

import java.util.List;

public class GameResult
{
  public List<Player> winners;
  public List<Player> losers;

  public GameResult(List<Player> winners, List<Player> losers)
  {
    this.winners = winners;
    this.losers = losers;
  }
}
