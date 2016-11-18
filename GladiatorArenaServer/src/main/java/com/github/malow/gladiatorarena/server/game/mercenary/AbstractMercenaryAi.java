package com.github.malow.gladiatorarena.server.game.mercenary;

import java.util.List;

import com.github.malow.gladiatorarena.server.game.action.Action;

public abstract class AbstractMercenaryAi extends Mercenary
{
  public abstract List<Action> updateAi();
}
