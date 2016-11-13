package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.accountserver.database.Database.UnexpectedException;
import com.github.malow.gladiatorarena.server.ErrorMessages;
import com.github.malow.gladiatorarena.server.comstructs.GetMyInfoResponse;
import com.github.malow.gladiatorarena.server.database.Player;
import com.github.malow.gladiatorarena.server.database.PlayerAccessor;
import com.github.malow.malowlib.MaloWLogger;

public class HttpsGameApiHandler
{
  public static Response getMyInfo(AuthorizedRequest req)
  {
    try
    {
      Player player = PlayerAccessor.read(req.accountId, req.email);
      return new GetMyInfoResponse(true, player);
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
      Player player = PlayerAccessor.read(req.accountId, req.email);
      if (player.currentMatchId != null) return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      if (player.isSearchingForGame) return new ErrorResponse(false, ErrorMessages.ALREADY_IN_QUEUE);

      player.isSearchingForGame = true;
      PlayerAccessor.updateCacheOnly(player);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakerHandler.queue(player);
      return new Response(true);
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
      Player player = PlayerAccessor.read(req.accountId, req.email);
      if (player.currentMatchId != null) return new ErrorResponse(false, ErrorMessages.ALREADY_HAVE_A_MATCH);
      if (!player.isSearchingForGame) return new ErrorResponse(false, ErrorMessages.NOT_IN_QUEUE);

      player.isSearchingForGame = false;
      PlayerAccessor.updateCacheOnly(player);
      //NamedMutex example, unlock. Also unlock before the returns above tho
      MatchmakerHandler.unqueue(player);
      return new Response(true);
    }
    catch (UnexpectedException e)
    {
      MaloWLogger.error("Unexpected error when trying to queueMatchmaking", e);
      return new ErrorResponse(false, "Unexpected error");
    }
  }
}
