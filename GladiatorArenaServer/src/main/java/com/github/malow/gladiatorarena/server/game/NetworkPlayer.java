package com.github.malow.gladiatorarena.server.game;

import com.github.malow.gladiatorarena.gamecore.Player;
import com.github.malow.gladiatorarena.gamecore.message.GameFinishedUpdate;
import com.github.malow.gladiatorarena.gamecore.message.GameStateUpdate;
import com.github.malow.gladiatorarena.server.database.User;
import com.github.malow.gladiatorarena.server.game.socketnetwork.Client;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage;
import com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs.GameMessage.GameMessageMethod;
import com.github.malow.malowlib.GsonSingleton;

public class NetworkPlayer extends Player
{
  public Client client;
  public Integer userId;
  public boolean ready = false;
  public boolean disconnected = false;

  public NetworkPlayer(User user, Client client)
  {
    super(user.username);
    this.userId = user.getId();
    this.client = client;
  }

  @Override
  public void gameStateUpdate()
  {
    GameMessage gameMessage = new GameMessage(GameMessageMethod.GAME_STATE_UPDATE, GsonSingleton.toJson(new GameStateUpdate()));
    this.client.sendData(GsonSingleton.toJson(gameMessage));
  }

  @Override
  public void gameFinishedUpdate(String winner)
  {
    GameMessage gameMessage = new GameMessage(GameMessageMethod.GAME_FINISHED_UPDATE, GsonSingleton.toJson(new GameFinishedUpdate(winner)));
    this.client.sendData(GsonSingleton.toJson(gameMessage));
  }
}
