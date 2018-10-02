package com.github.malow.gladiatorarena.server.database;

import java.util.List;

import com.github.malow.malowlib.database.Accessor;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;
import com.github.malow.malowlib.database.PreparedStatementPool;

public class MatchReferenceAccessor extends Accessor<MatchReference>
{
  private PreparedStatementPool readMultipleByUserIdStatements;
  private PreparedStatementPool readMultipleByMatchIdStatements;

  public MatchReferenceAccessor(DatabaseConnection databaseConnection)
  {
    super(databaseConnection);
    this.readMultipleByUserIdStatements = this.createPreparedStatementPool("SELECT * FROM " + this.tableName + " WHERE userId = ?");
    this.readMultipleByMatchIdStatements = this.createPreparedStatementPool("SELECT * FROM " + this.tableName + " WHERE matchId = ?");
  }

  public List<MatchReference> readMultipleByUser(Integer userId) throws ZeroRowsReturnedException, UnexpectedException
  {
    try
    {
      return this.readMultipleByUserIdStatements.useStatement(statement ->
      {
        statement.setInt(1, userId);
        return this.readMultipleWithPopulatedStatement(statement);
      });
    }
    catch (ZeroRowsReturnedException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw this.logAndCreateUnexpectedException(
          "Unexpected error when trying to read a " + this.entityClass.getSimpleName() + " with userId " + userId + " in accessor", e);
    }
  }

  public List<MatchReference> readMultipleByMatch(Integer matchId) throws ZeroRowsReturnedException, UnexpectedException
  {
    try
    {
      return this.readMultipleByMatchIdStatements.useStatement(statement ->
      {
        statement.setInt(1, matchId);
        return this.readMultipleWithPopulatedStatement(statement);
      });
    }
    catch (ZeroRowsReturnedException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw this.logAndCreateUnexpectedException(
          "Unexpected error when trying to read a " + this.entityClass.getSimpleName() + " with matchId " + matchId + " in accessor", e);
    }
  }
}
