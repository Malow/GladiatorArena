package com.github.malow.gladiatorarena.server.game.socketnetwork;

import java.net.Socket;

import com.github.malow.malowlib.malowprocess.ProcessEvent;
import com.github.malow.malowlib.network.tpcsocketmessage.MessageNetworkChannel;

public class Client extends MessageNetworkChannel
{
  public Integer userId;
  private boolean connected = true;

  public Client(Socket socket)
  {
    super(socket);
  }

  public boolean isConnected()
  {
    return this.connected;
  }

  @Override
  protected ProcessEvent createEvent(String msg)
  {
    return new GameNetworkPacket(this, msg);
  }

  @Override
  public void closeSpecific()
  {
    this.connected = false;
    super.closeSpecific();
  }
}
