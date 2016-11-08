package com.github.malow.gladiatorarena.server.game;

import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;

public class ConnectedPlayer
{
  public Player player;
  public Client client;

  public ConnectedPlayer(Player player)
  {
    this.player = player;
    this.client = null;
  }

  public boolean isReady()
  {
    return this.client != null && this.client.ready;
  }

  public void setReady(boolean ready)
  {
    if (this.client != null) this.client.ready = ready;
  }
}
