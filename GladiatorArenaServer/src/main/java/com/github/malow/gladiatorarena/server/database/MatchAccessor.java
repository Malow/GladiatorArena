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
      s.setInt(i++, match.status.id);
      s.setString(i++, match.createdAt.getTime().toString());
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

  public static boolean update(Match match) throws UnexpectedException
  {
    try
    {
      PreparedStatement s1 = Database.getConnection().prepareStatement(
          "UPDATE Matches SET player1_id = ?, player2_id = ?, username1 = ?, username2 = ?, rating_before_player1 = ?, rating_before_player2 = ?, status = ?, created_at = ?, winner_username = ?, rating_change_player1 = ?, rating_change_player2 = ?, finished_at = ? WHERE id = ?;");
      int i = 1;
      s1.setLong(i++, match.player1Id);
      s1.setLong(i++, match.player2Id);
      s1.setString(i++, match.username1);
      s1.setString(i++, match.username2);
      s1.setInt(i++, match.ratingBeforePlayer1);
      s1.setInt(i++, match.ratingBeforePlayer2);
      s1.setInt(i++, match.status.id);
      s1.setString(i++, match.createdAt.getTime().toString());
      s1.setString(i++, match.winnerUsername);
      s1.setInt(i++, match.ratingChangePlayer1);
      s1.setInt(i++, match.ratingChangePlayer2);
      if (match.finishedAt != null) s1.setString(i++, match.finishedAt.getTime().toString());
      else s1.setString(i++, null);
      s1.setLong(i++, match.id);
      int rowCount = s1.executeUpdate();
      s1.close();

      if (rowCount == 1) { return true; }
    }
    catch (Exception e)
    {
      UnexpectedException ue = new UnexpectedException(e.toString());
      ue.setStackTrace(e.getStackTrace());
      throw ue;
    }
    return false;
  }
}
