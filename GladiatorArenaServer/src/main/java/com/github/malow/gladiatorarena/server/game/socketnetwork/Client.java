package com.github.malow.gladiatorarena.server.game.socketnetwork;

import java.net.Socket;

import com.github.malow.malowlib.malowprocess.ProcessEvent;
import com.github.malow.malowlib.network.MessageNetworkChannel;

public class Client extends MessageNetworkChannel
{
  public Integer userId;

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
