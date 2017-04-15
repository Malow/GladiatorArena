package com.github.malow.gladiatorarena.server.comstructs;

import com.github.malow.accountserver.comstructs.AuthorizedRequest;

public class CreatePlayerRequest extends AuthorizedRequest
{
  public String username;

  public CreatePlayerRequest(String email, String authToken, String username)
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
