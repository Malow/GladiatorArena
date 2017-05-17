package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class ReadyMessage extends SocketMessage
{
  public String username;

  public ReadyMessage(String username)
  {
    super(SocketMethod.READY);
    this.username = username;
  }

  public ReadyMessage()
  {
    super(SocketMethod.READY);
  }

  @Override
  public boolean isValid()
  {
    if (super.isValid() && this.username != null)
    {
      return true;
    }
    return false;
  }
}
