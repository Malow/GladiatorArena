package com.github.malow.gladiatorarena.server.database;

import java.sql.PreparedStatement;
import java.util.List;

import com.github.malow.malowlib.database.Accessor;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;
import com.github.malow.malowlib.database.PreparedStatementPool;

public class MatchReferenceAccessor extends Accessor<MatchReference>
{
  private PreparedStatementPool readMultipleByPlayerIdStatements;
  private PreparedStatementPool readMultipleByMatchIdStatements;

  public MatchReferenceAccessor(DatabaseConnection databaseConnection)
  {
    super(databaseConnection);
    this.readMultipleByPlayerIdStatements = this.createPreparedStatementPool("SELECT * FROM " + this.tableName + " WHERE playerId = ?");
    this.readMultipleByMatchIdStatements = this.createPreparedStatementPool("SELECT * FROM " + this.tableName + " WHERE matchId = ?");
  }

  public List<MatchReference> readMultipleByPlayer(Integer playerId) throws ZeroRowsReturnedException, UnexpectedException
  {
    PreparedStatement statement = null;
    try
    {
      statement = this.readMultipleByPlayerIdStatements.get();
      statement.setInt(1, playerId);
      List<MatchReference> references = this.readMultipleWithPopulatedStatement(statement);
      this.readMultipleByPlayerIdStatements.add(statement);
      return references;
    }
    catch (ZeroRowsReturnedException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      this.closeStatement(statement);
      this.logAndReThrowUnexpectedException(
          "Unexpected error when trying to read a " + this.entityClass.getSimpleName() + " with playerId " + playerId + " in accessor", e);
    }
    return null;
  }

  public List<MatchReference> readMultipleByMatch(Integer matchId) throws ZeroRowsReturnedException, UnexpectedException
  {
    PreparedStatement statement = null;
    try
    {
      statement = this.readMultipleByMatchIdStatements.get();
      statement.setInt(1, matchId);
      List<MatchReference> references = this.readMultipleWithPopulatedStatement(statement);
      this.readMultipleByMatchIdStatements.add(statement);
      return references;
    }
    catch (ZeroRowsReturnedException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      this.closeStatement(statement);
      this.logAndReThrowUnexpectedException(
          "Unexpected error when trying to read a " + this.entityClass.getSimpleName() + " with matchId " + matchId + " in accessor", e);
    }
    return null;
  }
}
