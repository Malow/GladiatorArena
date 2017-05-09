package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.comstructs.CreateUserRequest;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.database.User;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.database.DatabaseExceptions.UnexpectedException;
import com.github.malow.malowlib.database.DatabaseExceptions.UniqueException;
import com.github.malow.malowlib.database.DatabaseExceptions.ZeroRowsReturnedException;

public class HttpsRequestHandler
{
  public static Response createUser(CreateUserRequest req)
  {
    try
    {
      User user = new User();
      user.accountId = req.accountId;
      user.username = req.username;
      UserAccessorSingleton.get().create(user);
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
        return new ErrorResponse(false, ErrorMessages.ACCOUNT_ALREADY_HAS_USER);
      }
    }
    catch (Exception e)
    {
      MaloWLogger.error("Unexpected error when trying to createUser", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }

  public static Response getMyInfo(AuthorizedRequest req)
  {
    try
    {
      User user = UserAccessorSingleton.get().readByAccountId(req.accountId);
      return new GetMyInfoResponse(true, user);
    }
    catch (ZeroRowsReturnedException e)
    {
      return new ErrorResponse(false, ErrorMessages.NO_USER_FOUND);
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
      //NamedMutex example, Lock for "user:#{accountId}", otherwise multiple queues can be done
      User user = UserAccessorSingleton.get().readByAccountId(req.accountId);
      if (user.currentGameId != null)
      {
        return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      }
      if (user.isSearchingForGame)
      {
        return new ErrorResponse(false, ErrorMessages.ALREADY_IN_QUEUE);
      }

      user.isSearchingForGame = true;
      UserAccessorSingleton.get().updateCacheOnly(user);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakingEngineSingleton.get().enqueue(user.getId(), user.rating);
      return new Response(true);
    }
    catch (ZeroRowsReturnedException e)
    {
      return new ErrorResponse(false, ErrorMessages.NO_USER_FOUND);
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
      //NamedMutex example, Lock for "user:#{accountId}", otherwise multiple queues can be done
      User user = UserAccessorSingleton.get().readByAccountId(req.accountId);
      if (user.currentGameId != null)
      {
        return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      }
      if (!user.isSearchingForGame)
      {
        return new ErrorResponse(false, ErrorMessages.NOT_IN_QUEUE);
      }

      user.isSearchingForGame = false;
      UserAccessorSingleton.get().updateCacheOnly(user);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakingEngineSingleton.get().dequeue(user.getId());
      return new Response(true);
    }
    catch (ZeroRowsReturnedException e)
    {
      return new ErrorResponse(false, ErrorMessages.NO_USER_FOUND);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Unexpected error when trying to queueMatchmaking", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }
}
