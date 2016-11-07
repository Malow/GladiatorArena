package com.github.malow.gladiatorarena.server.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.accountserver.database.AccountAccessor;
import com.github.malow.accountserver.database.Database;
import com.github.malow.accountserver.database.Database.UnexpectedException;
import com.mysql.jdbc.Statement;

public class PlayerAccessor
{
  private static ConcurrentHashMap<Long, Player> cacheByAccountId = new ConcurrentHashMap<Long, Player>();

  public static Player read(Long accountId, String email) throws UnexpectedException
  {
    Player a = cacheByAccountId.get(accountId);
    if (a != null) return a;

    try
    {
      PreparedStatement s1 = Database.getConnection().prepareStatement("SELECT * FROM Players WHERE account_id = ? ; ");
      s1.setLong(1, accountId);
      ResultSet s1Res = s1.executeQuery();

      if (s1Res.next())
      {
        Long id = s1Res.getLong("id");
        String username = s1Res.getString("username");
        Integer rating = s1Res.getInt("rating");

        Player player = new Player(id, accountId, username, rating);
        s1Res.close();
        s1.close();
        cacheByAccountId.put(accountId, player);
        return player;
      }
      else // No player found for that account, create one.
      {
        return create(new Player(AccountAccessor.read(email)));
      }
    }
    catch (Exception e)
    {
      UnexpectedException ue = new UnexpectedException(e.toString());
      ue.setStackTrace(e.getStackTrace());
      throw ue;
    }
  }

  public static Player create(Player player) throws UnexpectedException
  {
    try
    {
      PreparedStatement s = Database.getConnection().prepareStatement("insert into Players values (default, ?, ?, ?);",
          Statement.RETURN_GENERATED_KEYS);
      int i = 1;
      s.setLong(i++, player.accountId);
      s.setString(i++, player.username);
      s.setInt(i++, player.rating);
      int rowCount = s.executeUpdate();
      ResultSet generatedKeys = s.getGeneratedKeys();
      if (rowCount != 0 && generatedKeys.next())
      {
        player.id = generatedKeys.getLong(1);
        s.close();
        cacheByAccountId.put(player.accountId, player);
        return player;
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

  public static void updateCacheOnly(Player player)
  {
    cacheByAccountId.put(player.accountId, player);
  }

  public static void clearCache()
  {
    cacheByAccountId.clear();
  }
}
