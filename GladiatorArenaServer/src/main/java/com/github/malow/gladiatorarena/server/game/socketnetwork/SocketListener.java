package com.github.malow.gladiatorarena.server.game.socketnetwork;

import java.net.Socket;

import com.github.malow.gladiatorarena.server.handlers.MatchHandlerSingleton;
import com.github.malow.malowlib.network.NetworkChannel;
import com.github.malow.malowlib.network.NetworkServer;

public class SocketListener extends NetworkServer
{
  public SocketListener(int port)
  {
    super(port);
  }

  @Override
  public void clientConnected(NetworkChannel nc)
  {
    MatchHandlerSingleton.get().clientConnected(nc);
  }

  @Override
  public NetworkChannel createNetworkChannel(Socket socket)
  {
    return new Client(socket);
  }

}
