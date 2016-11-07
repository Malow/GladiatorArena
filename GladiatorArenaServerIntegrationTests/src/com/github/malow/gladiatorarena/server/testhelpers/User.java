package com.github.malow.gladiatorarena.server.testhelpers;

public class User
{
  public String email;
  public String username;
  public String password;
  public String authToken;

  public User(String email, String username, String password, String authToken)
  {
    this.email = email;
    this.username = username;
    this.password = password;
    this.authToken = authToken;
  }
}
