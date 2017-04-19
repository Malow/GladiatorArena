package com.github.malow.gladiatorarena.server.database;

import java.time.LocalDateTime;

import com.github.malow.malowlib.database.DatabaseTableEntity;

public class Match extends DatabaseTableEntity
{
  public LocalDateTime createdAt;

  public Match()
  {
    this.createdAt = LocalDateTime.now();
  }
}
