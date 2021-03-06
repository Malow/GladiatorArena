package com.github.malow.gladiatorarena.server.database;

import java.util.concurrent.ConcurrentHashMap;

import com.github.malow.malowlib.database.Accessor;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseExceptions.ForeignKeyException;
import com.github.malow.malowlib.database.DatabaseExceptions.MissingMandatoryFieldException;
import com.github.malow.malowlib.database.DatabaseExceptions.MultipleRowsReturnedException;
import com.github.malow.malowlib.database.DatabaseExceptions.SimultaneousModificationException;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.UniqueException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;
import com.github.malow.malowlib.database.PreparedStatementPool;

public class UserAccessor extends Accessor<User>
{
  private ConcurrentHashMap<Integer, User> cacheByAccountId = new ConcurrentHashMap<>();
  private PreparedStatementPool readByAccountIdStatements;

  public UserAccessor(DatabaseConnection databaseConnection)
  {
    super(databaseConnection);
    this.readByAccountIdStatements = this.createPreparedStatementPool("SELECT * FROM " + this.tableName + " WHERE accountId = ?");
  }

  @Override
  public User create(User user) throws UniqueException, ForeignKeyException, MissingMandatoryFieldException, UnexpectedException
  {
    user = super.create(user);
    this.cacheByAccountId.put(user.accountId, user);
    return user;
  }

  @Override
  public User read(Integer id) throws ZeroRowsReturnedException, MultipleRowsReturnedException, UnexpectedException
  {
    User user = super.read(id);
    this.cacheByAccountId.put(user.accountId, user);
    return user;
  }

  @Override
  public void update(User user) throws SimultaneousModificationException, MultipleRowsReturnedException, UnexpectedException
  {
    super.update(user);
    this.cacheByAccountId.put(user.accountId, user);
  }

  @Override
  public void delete(Integer id) throws ZeroRowsReturnedException, MultipleRowsReturnedException, UnexpectedException, ForeignKeyException
  {
    User user = super.read(id);
    super.delete(id);
    this.cacheByAccountId.remove(user);
  }

  public User readByAccountId(Integer accountId) throws ZeroRowsReturnedException, UnexpectedException
  {
    User user = this.cacheByAccountId.get(accountId);
    if (user != null)
    {
      return user;
    }
    try
    {
      user = this.readByAccountIdStatements.useStatement(statement ->
      {
        statement.setInt(1, accountId);
        return this.readWithPopulatedStatement(statement);
      });
      this.cacheByAccountId.put(user.accountId, user);
      return user;
    }
    catch (ZeroRowsReturnedException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw this.logAndCreateUnexpectedException(
          "Unexpected error when trying to read a " + this.entityClass.getSimpleName() + " with accountId " + accountId + " in accessor", e);
    }
  }

  public User readByGameToken(String gameToken)
  {
    return this.cacheByAccountId.values().stream().filter(u -> u.currentGameToken.equals(gameToken)).findAny().get();
  }

  public void updateCacheOnly(User user)
  {
    this.cacheByAccountId.put(user.accountId, user);
  }

  public void clearCache()
  {
    this.cacheByAccountId.clear();
  }
}
