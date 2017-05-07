package com.github.malow.gladiatorarena.gamecore;

import com.github.malow.gladiatorarena.gamecore.message.Message;

public abstract class Player
{
  public String username;
  public boolean hasFinishedTurn = false;

  public Player(String username)
  {
    this.username = username;
  }

  public abstract void handleMessage(Message message);
}
