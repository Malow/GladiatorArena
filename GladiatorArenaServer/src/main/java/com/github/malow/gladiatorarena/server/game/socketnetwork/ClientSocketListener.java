package com.github.malow.gladiatorarena.server.game.socketnetwork;

import java.net.Socket;

import com.github.malow.malowlib.malowprocess.MaloWProcess;
import com.github.malow.malowlib.network.NetworkChannel;
import com.github.malow.malowlib.network.SocketListener;

public class ClientSocketListener extends SocketListener
{
  public ClientSocketListener(int port, MaloWProcess notifier)
  {
    super(port, notifier);
  }

  @Override
  public NetworkChannel createNetworkChannel(Socket socket)
  {
    return new Client(socket);
  }

}
