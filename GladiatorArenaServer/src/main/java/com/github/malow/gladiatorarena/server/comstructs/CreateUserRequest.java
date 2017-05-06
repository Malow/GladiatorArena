package com.github.malow.gladiatorarena.server.comstructs;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;

public class CreateUserRequest extends AuthorizedRequest
{
  public String username;

  public CreateUserRequest(String email, String authToken, String username)
  {
    super(email, authToken);
    this.username = username;
  }

  @Override
  public boolean isValid()
  {
    if (super.isValid() && this.username != null)
    {
      return true;
    }
    return false;
  }
}
