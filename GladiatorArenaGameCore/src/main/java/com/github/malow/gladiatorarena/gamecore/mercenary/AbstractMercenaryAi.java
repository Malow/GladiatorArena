package com.github.malow.gladiatorarena.gamecore.mercenary;

import java.util.List;

import com.github.malow.gladiatorarena.gamecore.Action;

public abstract class AbstractMercenaryAi extends Mercenary
{
  public abstract List<Action> updateAi();
}
