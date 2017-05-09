package com.github.malow.gladiatorarena.server.database;

import com.github.malow.malowlib.database.DatabaseTableEntity;

public class MatchReference extends DatabaseTableEntity
{
  @ForeignKey(target = User.class)
  public Integer userId;
  @ForeignKey(target = Match.class)
  public Integer matchId;
  public Boolean isWinner;
  public Double ratingBefore;
  public Double ratingChange;
  public String username;
}
