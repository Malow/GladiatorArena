package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;
import com.github.malow.gladiatorarena.server.comstructs.CreateUserRequest;
import com.github.malow.malowlib.network.https.HttpsJsonPostHandler;
import com.github.malow.malowlib.network.https.HttpsPostResponse;

public class UserHttpsHandlers
{
  public static class CreateUserHandler extends HttpsJsonPostHandler<CreateUserRequest>
  {
    @Override
    public HttpsPostResponse handleRequestAndGetResponse(CreateUserRequest request) throws BadRequestException
    {
      return UserRequestHandler.createUser(request);
    }
  }

  public static class GetMyInfoHandler extends HttpsJsonPostHandler<AuthorizedRequest>
  {
    @Override
    public HttpsPostResponse handleRequestAndGetResponse(AuthorizedRequest request) throws BadRequestException
    {
      return UserRequestHandler.getMyInfo(request);
    }
  }

  public static class QueueMatchmakingHandler extends HttpsJsonPostHandler<AuthorizedRequest>
  {
    @Override
    public HttpsPostResponse handleRequestAndGetResponse(AuthorizedRequest request) throws BadRequestException
    {
      return UserRequestHandler.queueMatchmaking(request);
    }
  }

  public static class UnqueueMatchmakingHandler extends HttpsJsonPostHandler<AuthorizedRequest>
  {
    @Override
    public HttpsPostResponse handleRequestAndGetResponse(AuthorizedRequest request) throws BadRequestException
    {
      return UserRequestHandler.unqueueMatchmaking(request);
    }
  }
}
