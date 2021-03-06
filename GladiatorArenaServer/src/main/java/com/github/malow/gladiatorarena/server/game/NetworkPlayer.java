package com.github.malow.gladiatorarena.server.game;

import com.github.malow.gladiatorarena.gamecore.Player;
import com.github.malow.gladiatorarena.gamecore.message.Message;
import com.github.malow.gladiatorarena.server.database.User;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.SocketMessage;
import com.github.malow.malowlib.GsonSingleton;

public class NetworkPlayer extends Player
{
  public Client client;
  public Integer userId;
  public boolean ready = false;
  public String gameToken;

  public NetworkPlayer(User user, Client client, String gameToken)
  {
    super(user.username);
    this.userId = user.getId();
    this.client = client;
    this.gameToken = gameToken;
  }

  public boolean isConnected()
  {
    return this.client.isConnected();
  }

  @Override
  public void handleMessage(Message message)
  {
    this.client.sendMessage(GsonSingleton.toJson(new GameMessage(message)));
  }

  public void sendMessage(SocketMessage message)
  {
    this.client.sendMessage(GsonSingleton.toJson(message));
  }
}
