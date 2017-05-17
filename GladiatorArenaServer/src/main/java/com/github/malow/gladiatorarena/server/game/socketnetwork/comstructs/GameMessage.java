package com.github.malow.gladiatorarena.server.game.socketnetwork.comstructs;

import com.github.malow.gladiatorarena.gamecore.message.AttackAction;
import com.github.malow.gladiatorarena.gamecore.message.FinishTurn;
import com.github.malow.gladiatorarena.gamecore.message.GameFinishedUpdate;
import com.github.malow.gladiatorarena.gamecore.message.GameStateInformation;
import com.github.malow.gladiatorarena.gamecore.message.Message;
import com.github.malow.gladiatorarena.gamecore.message.MoveAction;
import com.github.malow.gladiatorarena.gamecore.message.NextTurn;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.MaloWLogger;

public class GameMessage extends SocketMessage
{
  public static enum GameMessageMethod
  {
    MOVE_ACTION,
    ATTACK_ACTION,
    FINISH_TURN,
    NEXT_TURN,
    GAME_STATE_UPDATE,
    GAME_FINISHED_UPDATE
  }

  private String messageJson;
  private GameMessageMethod gameMethod;

  public GameMessage(Message message)
  {
    super(SocketMethod.GAME_MESSAGE);
    if (message instanceof MoveAction)
    {
      this.gameMethod = GameMessageMethod.MOVE_ACTION;
    }
    else if (message instanceof AttackAction)
    {
      this.gameMethod = GameMessageMethod.ATTACK_ACTION;
    }
    else if (message instanceof FinishTurn)
    {
      this.gameMethod = GameMessageMethod.FINISH_TURN;
    }
    else if (message instanceof NextTurn)
    {
      this.gameMethod = GameMessageMethod.NEXT_TURN;
    }
    else if (message instanceof GameFinishedUpdate)
    {
      this.gameMethod = GameMessageMethod.GAME_FINISHED_UPDATE;
    }
    else if (message instanceof GameStateInformation)
    {
      this.gameMethod = GameMessageMethod.GAME_STATE_UPDATE;
    }
    else
    {
      MaloWLogger.error("Unknown type of message: " + message.getClass().getSimpleName(), new Exception());
    }
    this.messageJson = GsonSingleton.toJson(message);
  }

  public Message getMessage()
  {
    switch (this.gameMethod)
    {
      case MOVE_ACTION:
        return GsonSingleton.fromJson(this.messageJson, MoveAction.class);
      case ATTACK_ACTION:
        return GsonSingleton.fromJson(this.messageJson, AttackAction.class);
      case FINISH_TURN:
        return GsonSingleton.fromJson(this.messageJson, FinishTurn.class);
      case NEXT_TURN:
        return GsonSingleton.fromJson(this.messageJson, NextTurn.class);
      case GAME_FINISHED_UPDATE:
        return GsonSingleton.fromJson(this.messageJson, GameFinishedUpdate.class);
      case GAME_STATE_UPDATE:
        return GsonSingleton.fromJson(this.messageJson, GameStateInformation.class);
      default:
        MaloWLogger.error("Recieved a GameMessage with unkown method: " + this.gameMethod, new Exception());
        return null;
    }
  }
}
