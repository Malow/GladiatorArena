package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.accountserver.comstructs.ErrorResponse;
import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.network.https.HttpsPostHandler;

public class HttpsHandlers
{
  public static class GetMyInfoHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request)
    {
      AuthorizedRequest req = (AuthorizedRequest) createValidJsonRequest(request, AuthorizedRequest.class);
      if (req != null)
      {
        Response resp = PlayerHandler.getMyInfo(req);
        return GsonSingleton.get().toJson(resp);
      }
      else
      {
        return GsonSingleton.get().toJson(new ErrorResponse(false, "Request has wrong parameters"));
      }
    }
  }

  public static class QueueMatchmakingHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request)
    {
      AuthorizedRequest req = (AuthorizedRequest) createValidJsonRequest(request, AuthorizedRequest.class);
      if (req != null)
      {
        Response resp = PlayerHandler.queueMatchmaking(req);
        return GsonSingleton.get().toJson(resp);
      }
      else
      {
        return GsonSingleton.get().toJson(new ErrorResponse(false, "Request has wrong parameters"));
      }
    }
  }
}
