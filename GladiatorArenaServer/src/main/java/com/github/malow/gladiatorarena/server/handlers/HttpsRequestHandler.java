package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.comstructs.CreatePlayerRequest;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.database.PlayerAccessorSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.UniqueException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;

public class HttpsRequestHandler
{
  public static Response createPlayer(CreatePlayerRequest req)
  {
    try
    {
      Player player = new Player();
      player.accountId = req.accountId;
      player.username = req.username;
      PlayerAccessorSingleton.get().create(player);
      return new Response(true);
    }
    catch (UniqueException e)
    {
      if (e.fieldName.equals("username"))
      {
        return new ErrorResponse(false, ErrorMessages.USERNAME_TAKEN);
      }
      else
      {
        return new ErrorResponse(false, ErrorMessages.ACCOUNT_ALREADY_HAS_PLAYER);
      }
    }
    catch (Exception e)
    {
      MaloWLogger.error("Unexpected error when trying to createPlayer", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }

  public static Response getMyInfo(AuthorizedRequest req)
  {
    try
    {
      Player player = PlayerAccessorSingleton.get().readByAccountId(req.accountId);
      return new GetMyInfoResponse(true, player);
    }
    catch (ZeroRowsReturnedException e)
    {
      return new ErrorResponse(false, ErrorMessages.NO_PLAYER_FOUND);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Unexpected error when trying to getMyInfo", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }

  public static Response queueMatchmaking(AuthorizedRequest req)
  {
    try
    {
      //NamedMutex example, Lock for "player:#{accountId}", otherwise multiple queues can be done
      Player player = PlayerAccessorSingleton.get().readByAccountId(req.accountId);
      if (player.currentMatchId != null)
      {
        return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      }
      if (player.isSearchingForGame)
      {
        return new ErrorResponse(false, ErrorMessages.ALREADY_IN_QUEUE);
      }

      player.isSearchingForGame = true;
      PlayerAccessorSingleton.get().updateCacheOnly(player);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakerHandler.queue(player);
      return new Response(true);
    }
    catch (ZeroRowsReturnedException e)
    {
      return new ErrorResponse(false, ErrorMessages.NO_PLAYER_FOUND);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Unexpected error when trying to queueMatchmaking", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }

  public static Response unqueueMatchmaking(AuthorizedRequest req)
  {
    try
    {
      //NamedMutex example, Lock for "player:#{accountId}", otherwise multiple queues can be done
      Player player = PlayerAccessorSingleton.get().readByAccountId(req.accountId);
      if (player.currentMatchId != null)
      {
        return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      }
      if (!player.isSearchingForGame)
      {
        return new ErrorResponse(false, ErrorMessages.NOT_IN_QUEUE);
      }

      player.isSearchingForGame = false;
      PlayerAccessorSingleton.get().updateCacheOnly(player);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakerHandler.unqueue(player);
      return new Response(true);
    }
    catch (ZeroRowsReturnedException e)
    {
      return new ErrorResponse(false, ErrorMessages.NO_PLAYER_FOUND);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Unexpected error when trying to queueMatchmaking", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }
}
