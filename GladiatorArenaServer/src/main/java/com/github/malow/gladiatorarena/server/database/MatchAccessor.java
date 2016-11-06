package com.github.malow.gladiatorarena.server.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.github.malow.accountserver.database.Database;
import com.github.malow.accountserver.database.Database.UnexpectedException;
import com.mysql.jdbc.Statement;

public class MatchAccessor
{
  public static Match create(Match match) throws UnexpectedException
  {
    try
    {
      PreparedStatement s = Database.getConnection().prepareStatement("insert into Matches values (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
          Statement.RETURN_GENERATED_KEYS);
      int i = 1;
      s.setLong(i++, match.player1Id);
      s.setLong(i++, match.player2Id);
      s.setString(i++, match.username1);
      s.setString(i++, match.username2);
      s.setInt(i++, match.ratingBeforePlayer1);
      s.setInt(i++, match.ratingBeforePlayer2);
      s.setInt(i++, match.status);
      s.setString(i++, match.created.toString());
      s.setObject(i++, null);
      s.setObject(i++, null);
      s.setObject(i++, null);
      s.setString(i++, null);
      int rowCount = s.executeUpdate();
      ResultSet generatedKeys = s.getGeneratedKeys();
      if (rowCount != 0 && generatedKeys.next())
      {
        match.id = generatedKeys.getLong(1);
        s.close();
        return match;
      }
    }
    catch (Exception e)
    {
      UnexpectedException ue = new UnexpectedException(e.toString());
      ue.setStackTrace(e.getStackTrace());
      throw ue;
    }
    return null;
  }
}
