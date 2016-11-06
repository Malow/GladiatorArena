package com.github.malow.gladiatorarena.server.database;

import java.util.Date;

public class Match
{
  public static class MatchStatus
  {
    public static final int NOT_STARTED = 0;
    public static final int IN_PROGRESS = 1;
    public static final int COMPLETED = 2;
  }

  // Persisted in database
  public Long id;
  public Long player1Id;
  public Long player2Id;
  public String username1;
  public String username2;
  public Integer ratingBeforePlayer1;
  public Integer ratingBeforePlayer2;
  public Date created;
  public Integer status;
  public boolean player1Won;
  public Integer ratingChangePlayer1;
  public Integer ratingChangePlayer2;
  public Date finished;

  public Match(Long id, Player p1, Player p2)
  {
    this.id = id;
    this.player1Id = p1.id;
    this.player2Id = p2.id;
    this.username1 = p1.username;
    this.username2 = p2.username;
    this.ratingBeforePlayer1 = p1.rating;
    this.ratingBeforePlayer2 = p2.rating;
    this.status = MatchStatus.NOT_STARTED;
    this.created = new Date();
  }
}
