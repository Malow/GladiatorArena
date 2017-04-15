package com.github.malow.gladiatorarena.server.database;

import java.sql.PreparedStatement;
import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.malowlib.database.Accessor;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseExceptions.ForeignKeyException;
import com.github.malow.malowlib.database.DatabaseExceptions.MissingMandatoryFieldException;
import com.github.malow.malowlib.database.DatabaseExceptions.MultipleRowsReturnedException;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.UniqueException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;
import com.github.malow.malowlib.database.PreparedStatementPool;

public class PlayerAccessor extends Accessor<Player>
{
  private ConcurrentHashMap<Integer, Player> cacheByAccountId = new ConcurrentHashMap<Integer, Player>();
  private PreparedStatementPool readByAccountIdStatements;

  public PlayerAccessor(DatabaseConnection databaseConnection)
  {
    super(databaseConnection, Player.class);
    this.readByAccountIdStatements = this.createPreparedStatementPool("SELECT * FROM " + this.tableName + " WHERE accountId = ?");
  }

  @Override
  public Player create(Player player) throws UniqueException, ForeignKeyException, MissingMandatoryFieldException, UnexpectedException
  {
    player = super.create(player);
    this.cacheByAccountId.put(player.accountId, player);
    return player;
  }

  @Override
  public Player read(Integer id) throws ZeroRowsReturnedException, MultipleRowsReturnedException, UnexpectedException
  {
    Player player = super.read(id);
    this.cacheByAccountId.put(player.accountId, player);
    return player;
  }

  @Override
  public void update(Player player) throws ZeroRowsReturnedException, MultipleRowsReturnedException, UnexpectedException
  {
    super.update(player);
    this.cacheByAccountId.put(player.accountId, player);
  }

  @Override
  public void delete(Integer id) throws ZeroRowsReturnedException, MultipleRowsReturnedException, UnexpectedException
  {
    Player player = super.read(id);
    super.delete(id);
    this.cacheByAccountId.remove(player);
  }

  public Player readByAccountId(Integer accountId) throws ZeroRowsReturnedException, UnexpectedException
  {
    Player player = this.cacheByAccountId.get(accountId);
    if (player != null)
    {
      return player;
    }
    PreparedStatement statement = null;
    try
    {
      statement = this.readByAccountIdStatements.get();
      statement.setInt(1, accountId);
      player = this.readWithPopulatedStatement(statement);
      this.cacheByAccountId.put(player.accountId, player);
      this.readByAccountIdStatements.add(statement);
      return player;
    }
    catch (ZeroRowsReturnedException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      this.closeStatement(statement);
      this.logAndReThrowUnexpectedException(
          "Unexpected error when trying to read a " + this.entityClass.getSimpleName() + " with accountId " + accountId + " in accessor", e);
    }
    return null;
  }

  public void updateCacheOnly(Player player)
  {
    this.cacheByAccountId.put(player.accountId, player);
  }

  public void clearCache()
  {
    this.cacheByAccountId.clear();
  }
}
