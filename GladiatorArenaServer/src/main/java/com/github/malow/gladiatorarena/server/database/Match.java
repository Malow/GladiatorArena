package com.github.malow.gladiatorarena.server.database;

import java.time.LocalDateTime;

import com.github.malow.gladiatorarena.server.game.GameStatus;
import com.github.malow.malowlib.database.DatabaseTableEntity;

public class Match extends DatabaseTableEntity
{
  @ForeignKey(target = Player.class)
  public Integer player1Id;
  @ForeignKey(target = Player.class)
  public Integer player2Id;
  public String username1;
  public String username2;
  public Integer ratingBeforePlayer1;
  public Integer ratingBeforePlayer2;
  public LocalDateTime createdAt;
  public GameStatus status;
  @Optional
  public String winnerUsername;
  @Optional
  public Integer ratingChangePlayer1;
  @Optional
  public Integer ratingChangePlayer2;
  @Optional
  public LocalDateTime finishedAt;

  public Match(Player p1, Player p2)
  {
    this.player1Id = p1.getId();
    this.player2Id = p2.getId();
    this.username1 = p1.username;
    this.username2 = p2.username;
    this.ratingBeforePlayer1 = p1.rating;
    this.ratingBeforePlayer2 = p2.rating;
    this.status = GameStatus.NOT_STARTED;
    this.createdAt = LocalDateTime.now();
  }
}
