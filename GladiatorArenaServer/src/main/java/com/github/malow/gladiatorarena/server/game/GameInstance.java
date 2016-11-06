package com.github.malow.gladiatorarena.server.game;

import com.github.malow.gladiatorarena.server.database.Match;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.malowlib.MaloWProcess;
import com.github.malow.malowlib.ProcessEvent;

public class GameInstance extends MaloWProcess
{
  private Player p1;
  private Player p2;
  private Match match;
  private Client c1;
  private Client c2;

  public GameInstance(Player p1, Player p2, Match match)
  {
    this.p1 = p1;
    this.p2 = p2;
    this.match = match;
  }

  public boolean clientConnected(Client client)
  {
    if (client.accId.equals(this.p1.accountId))
    {
      if (this.c1 != null)
      {
        this.c1.setNotifier(null);
        this.c1.close();
      }
      this.c1 = client;
      return true;
    }
    else if (client.accId.equals(this.p2.accountId))
    {
      if (this.c2 != null)
      {
        this.c2.setNotifier(null);
        this.c2.close();
      }
      this.c2 = client;
      return true;
    }
    return false;
  }

  @Override
  public void life()
  {
    while (this.stayAlive)
    {
      ProcessEvent ev = this.waitEvent();
    }
  }

  @Override
  public void closeSpecific()
  {
  }
}
