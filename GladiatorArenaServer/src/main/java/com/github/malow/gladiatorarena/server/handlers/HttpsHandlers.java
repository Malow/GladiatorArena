package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.PlayerAccessor;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.HttpsPostHandler;

public class HttpsHandlers
{
  public static class ClearCacheHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request)
    {
      PlayerAccessor.clearCache();
      MatchmakerHandler.clearQueue();
      return GsonSingleton.toJson(new Response(true));
    }
  }

  public static class GetMyInfoHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      AuthorizedRequest req = (AuthorizedRequest) createValidJsonRequest(request, AuthorizedRequest.class);
      Response resp = HttpsGameApiHandler.getMyInfo(req);
      return GsonSingleton.toJson(resp);
    }
  }

  public static class QueueMatchmakingHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      AuthorizedRequest req = (AuthorizedRequest) createValidJsonRequest(request, AuthorizedRequest.class);
      Response resp = HttpsGameApiHandler.queueMatchmaking(req);
      return GsonSingleton.toJson(resp);
    }
  }

  public static class UnqueueMatchmakingHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      AuthorizedRequest req = (AuthorizedRequest) createValidJsonRequest(request, AuthorizedRequest.class);
      Response resp = HttpsGameApiHandler.unqueueMatchmaking(req);
      return GsonSingleton.toJson(resp);
    }
  }
}
