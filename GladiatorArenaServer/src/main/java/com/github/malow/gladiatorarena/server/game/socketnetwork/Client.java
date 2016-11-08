package com.github.malow.gladiatorarena.server.game.socketnetwork;

import java.net.Socket;

import com.github.malow.malowlib.ProcessEvent;
import com.github.malow.malowlib.network.NetworkChannel;

public class Client extends NetworkChannel
{
  public Long accId;
  public String email;
  public String authToken;

  // Game logic
  public boolean ready;

  public Client(Socket socket)
  {
    super(socket);
    this.accId = null;
    this.email = null;
    this.authToken = null;
  }

  public Client(Socket socket, Long accId, String email, String authToken)
  {
    super(socket);
    this.accId = accId;
    this.email = email;
    this.authToken = authToken;
  }

  @Override
  protected ProcessEvent createEvent(String msg)
  {
    return new GameNetworkPacket(this, msg);
  }
}
