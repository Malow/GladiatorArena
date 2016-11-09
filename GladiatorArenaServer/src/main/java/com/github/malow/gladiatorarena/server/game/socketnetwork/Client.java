package com.github.malow.gladiatorarena.server.game.socketnetwork;

import java.net.Socket;

import com.github.malow.malowlib.ProcessEvent;
import com.github.malow.malowlib.network.NetworkChannel;

public class Client extends NetworkChannel
{
  public boolean ready = false;
  public Long accId;

  public boolean disconnected = false;

  public Client(Socket socket)
  {
    super(socket);
  }

  @Override
  protected ProcessEvent createEvent(String msg)
  {
    return new GameNetworkPacket(this, msg);
  }
}
