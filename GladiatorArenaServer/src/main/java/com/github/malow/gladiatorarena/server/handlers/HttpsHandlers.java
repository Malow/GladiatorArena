package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.comstructs.CreateUserRequest;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.HttpsPostHandler;

public class HttpsHandlers
{
  public static class ClearCacheHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request)
    {
      UserAccessorSingleton.get().clearCache();
      MatchmakerHandler.clearQueue();
      return GsonSingleton.toJson(new Response(true));
    }
  }

  public static class CreateUserHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      CreateUserRequest req = createValidJsonRequest(request, CreateUserRequest.class);
      Response resp = HttpsRequestHandler.createUser(req);
      return GsonSingleton.toJson(resp);
    }
  }

  public static class GetMyInfoHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      AuthorizedRequest req = createValidJsonRequest(request, AuthorizedRequest.class);
      Response resp = HttpsRequestHandler.getMyInfo(req);
      return GsonSingleton.toJson(resp);
    }
  }

  public static class QueueMatchmakingHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      AuthorizedRequest req = createValidJsonRequest(request, AuthorizedRequest.class);
      Response resp = HttpsRequestHandler.queueMatchmaking(req);
      return GsonSingleton.toJson(resp);
    }
  }

  public static class UnqueueMatchmakingHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request) throws BadRequestException
    {
      AuthorizedRequest req = createValidJsonRequest(request, AuthorizedRequest.class);
      Response resp = HttpsRequestHandler.unqueueMatchmaking(req);
      return GsonSingleton.toJson(resp);
    }
  }
}
