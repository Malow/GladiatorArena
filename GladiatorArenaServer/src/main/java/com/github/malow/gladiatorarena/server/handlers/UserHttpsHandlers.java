package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.gladiatorarena.server.comstructs.CreateUserRequest;
import com.github.malow.malowlib.network.https.HttpRequestHandler;
import com.github.malow.malowlib.network.https.HttpResponse;

public class UserHttpsHandlers
{
  public static class CreateUserHandler extends HttpRequestHandler<CreateUserRequest>
  {
    @Override
    public HttpResponse handleRequestAndGetResponse(CreateUserRequest request) throws BadRequestException
    {
      return UserRequestHandler.createUser(request);
    }
  }

  public static class GetMyInfoHandler extends HttpRequestHandler<AuthorizedRequest>
  {
    @Override
    public HttpResponse handleRequestAndGetResponse(AuthorizedRequest request) throws BadRequestException
    {
      return UserRequestHandler.getMyInfo(request);
    }
  }

  public static class QueueMatchmakingHandler extends HttpRequestHandler<AuthorizedRequest>
  {
    @Override
    public HttpResponse handleRequestAndGetResponse(AuthorizedRequest request) throws BadRequestException
    {
      return UserRequestHandler.queueMatchmaking(request);
    }
  }

  public static class UnqueueMatchmakingHandler extends HttpRequestHandler<AuthorizedRequest>
  {
    @Override
    public HttpResponse handleRequestAndGetResponse(AuthorizedRequest request) throws BadRequestException
    {
      return UserRequestHandler.unqueueMatchmaking(request);
    }
  }
}
