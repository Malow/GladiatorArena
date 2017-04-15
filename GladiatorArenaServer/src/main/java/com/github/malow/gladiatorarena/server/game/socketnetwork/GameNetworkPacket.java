package com.github.malow.gladiatorarena.server.game.socketnetwork;

import com.github.malow.malowlib.malowprocess.ProcessEvent;

public class GameNetworkPacket extends ProcessEvent
{
  public Client client;
  public String message;

  public GameNetworkPacket(Client client, String message)
  {
    this.client = client;
    this.message = message;
  }
}
