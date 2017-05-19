package com.github.malow.gladiatorarena.gamecore;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

import com.github.malow.gladiatorarena.gamecore.hex.Unit;

public class UnitOrder
{
  private Queue<Unit> queue = new LinkedList<>();
  private LocalDateTime turnStartedAt = LocalDateTime.now();

  public void add(Unit unit)
  {
    this.queue.add(unit);
  }

  public Unit getCurrent()
  {
    return this.queue.peek();
  }

  public Unit rotate()
  {
    this.turnStartedAt = LocalDateTime.now();
    Unit unit = this.queue.poll();
    this.queue.add(unit);
    return this.queue.peek();
  }

  public void resetTurnTimer()
  {
    this.turnStartedAt = LocalDateTime.now();
  }

  public boolean hasTurnTimedOut()
  {
    return this.turnStartedAt.plusSeconds(GladiatorArenaGameConfig.GAME_TURN_TIMEOUT_SECONDS).isBefore(LocalDateTime.now());
  }
}
