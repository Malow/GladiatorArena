package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

public class GameMessage extends SocketMessage
{
  public static enum GameMessageMethod
  {
    ACTION,
    FINISH_TURN,
    GAME_STATE_UPDATE,
    GAME_FINISHED_UPDATE
  }

  public String messageJson;
  public GameMessageMethod gameMethod;

  public GameMessage(GameMessageMethod method, String messageJson)
  {
    super(SocketMethod.GAME_MESSAGE);
    this.gameMethod = method;
    this.messageJson = messageJson;
  }
}
