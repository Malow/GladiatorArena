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
      MaloWLogger.info("CreateUser request from " + req.email + " with username " + req.username + " was successful.");
      return new Response(true);
    }
    catch (UniqueException e)
    {
      if (e.fieldName.equals("username"))
      {
        MaloWLogger.info("CreateUser request from " + req.email + " with username " + req.username + " failed due to username being taken.");
        return new ErrorResponse(false, ErrorMessages.USERNAME_TAKEN);
      }
      else
      {
        MaloWLogger.info("CreateUser request from " + req.email + " with username " + req.username + " failed due to account already having a user.");
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
      MaloWLogger.info("GetMyInfo request from " + req.email + "/" + user.username + " was successful.");
      return new GetMyInfoResponse(true, user);
    }
    catch (ZeroRowsReturnedException e)
    {
      MaloWLogger.info("GetMyInfo request from " + req.email + " failed due to no user existing for the acocunt.");
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
        MaloWLogger
            .info("QueueMatchmaking request from " + req.email + "/" + user.username + " failed due to the user already having an active match.");
        return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      }
      if (user.isSearchingForGame)
      {
        MaloWLogger
            .info("QueueMatchmaking request from " + req.email + "/" + user.username + " failed due to the user already searching for a match.");
        return new ErrorResponse(false, ErrorMessages.ALREADY_IN_QUEUE);
      }

      user.isSearchingForGame = true;
      UserAccessorSingleton.get().updateCacheOnly(user);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakingEngineSingleton.get().enqueue(user.getId(), user.rating);
      MaloWLogger.info("QueueMatchmaking request from " + req.email + "/" + user.username + " was successful.");
      return new Response(true);
    }
    catch (ZeroRowsReturnedException e)
    {
      MaloWLogger.info("QueueMatchmaking request from " + req.email + " failed due to no user existing for the acocunt.");
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
      if (!user.isSearchingForGame)
      {
        MaloWLogger.info("UnqueueMatchmaking request from " + req.email + "/" + user.username + " failed due to the user not being in queue.");
        return new ErrorResponse(false, ErrorMessages.NOT_IN_QUEUE);
      }

      user.isSearchingForGame = false;
      UserAccessorSingleton.get().updateCacheOnly(user);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakingEngineSingleton.get().dequeue(user.getId());
      MaloWLogger.info("UnqueueMatchmaking request from " + req.email + "/" + user.username + " was successful.");
      return new Response(true);
    }
    catch (ZeroRowsReturnedException e)
    {
      MaloWLogger.info("UnqQueueMatchmaking request from " + req.email + " failed due to no user existing for the acocunt.");
      return new ErrorResponse(false, ErrorMessages.NO_USER_FOUND);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Unexpected error when trying to queueMatchmaking", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }
}
