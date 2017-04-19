package com.github.malow.gladiatorarena.server.game;

import java.util.List;

import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;

public class GameResult
{
  public List<Client> winners;
  public List<Client> losers;

  public GameResult(List<Client> winners, List<Client> losers)
  {
    this.winners = winners;
    this.losers = losers;
  }
}
