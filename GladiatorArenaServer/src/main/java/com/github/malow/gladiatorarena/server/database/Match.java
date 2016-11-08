package com.github.malow.gladiatorarena.server.database;

import java.util.Calendar;

import com.github.malow.gladiatorarena.server.game.GameStatus;

public class Match
{
  // Persisted in database
  public Long id;
  public Long player1Id;
  public Long player2Id;
  public String username1;
  public String username2;
  public Integer ratingBeforePlayer1;
  public Integer ratingBeforePlayer2;
  public Calendar createdAt;
  public GameStatus status;
  public String winnerUsername;
  public Integer ratingChangePlayer1;
  public Integer ratingChangePlayer2;
  public Calendar finishedAt;

  public Match(Long id, Player p1, Player p2)
  {
    this.id = id;
    this.player1Id = p1.id;
    this.player2Id = p2.id;
    this.username1 = p1.username;
    this.username2 = p2.username;
    this.ratingBeforePlayer1 = p1.rating;
    this.ratingBeforePlayer2 = p2.rating;
    this.status = GameStatus.NOT_STARTED;
    this.createdAt = Calendar.getInstance();
  }
}
