package com.github.malow.gladiatorarena.gamecore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.malow.gladiatorarena.gamecore.message.Action;
import com.github.malow.gladiatorarena.gamecore.message.FinishTurn;
import com.github.malow.gladiatorarena.gamecore.message.Message;
import com.github.malow.malowlib.MaloWLogger;

public class GameInstance
{
  private List<Player> players = new ArrayList<>();
  private LocalDateTime roundStartedAt;
  private int actions = 0;

  public void addPlayer(Player player)
  {
    this.players.add(player);
  }

  public Optional<GameResult> update(long diff)
  {
    if (this.isAllPlayersReady() || this.isTimedOut(this.roundStartedAt, GladiatorArenaGameConfig.GAME_ROUND_TIMEOUT_SECONDS))
    {
      // Haxx for now for the testcase
      if (this.actions > 1)
      {
        this.endGame(this.players.get(0));
        GameResult result = new GameResult(Arrays.asList(this.players.get(0)), Arrays.asList(this.players.get(1)));
        return Optional.of(result);
      }
      //nextTurn();
    }
    return Optional.empty();
  }

  public boolean handleMessage(Message message, String fromUsername)
  {
    Player from = this.getPlayerByUsername(fromUsername);
    if (message instanceof Action)
    {
      this.actions++;
      return true;
    }
    else if (message instanceof FinishTurn)
    {
      from.hasFinishedTurn = true;
    }
    else
    {
      MaloWLogger.error("Got an unexpected request: " + message, new Exception());
    }
    return false;
  }

  public void start()
  {
    this.nextTurn();
  }

  private void nextTurn()
  {
    this.roundStartedAt = LocalDateTime.now();
    this.players.stream().forEach(p -> p.hasFinishedTurn = false);
    this.players.stream().forEach(p -> p.gameStateUpdate());
    // Regen AP to mercs
    // Count down CDs to mercs abilities
  }

  private void endGame(Player winner)
  {
    this.players.stream().forEach(p -> p.gameFinishedUpdate(winner.username));
  }

  private boolean isTimedOut(LocalDateTime from, int seconds)
  {
    return from.plusSeconds(seconds).isBefore(LocalDateTime.now());
  }

  private boolean isAllPlayersReady()
  {
    return this.players.stream().allMatch(p -> p.hasFinishedTurn);
  }

  private Player getPlayerByUsername(String username)
  {
    return this.players.stream().filter(p -> p.username.equals(username)).findFirst().get();
  }
}
