package com.github.malow.gladiatorarena.gamecore.message;

import java.util.List;

public class GameFinishedUpdate extends Message
{
  public List<String> winners;

  public GameFinishedUpdate(List<String> winners)
  {
    this.winners = winners;
  }
}
