package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.malowlib.GsonSingleton;
import com.github.malow.malowlib.malowprocess.MaloWProcess.ProcessState;
import com.github.malow.malowlib.network.https.HttpsPostHandler;

public class TestHttpsHandlers
{
  public static class ClearCacheHandler extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request)
    {
      UserAccessorSingleton.get().clearCache();
      MatchmakingEngineSingleton.get().clearQueue();
      return GsonSingleton.toJson(new Response(true));
    }
  }

  public static class WaitForEmptyMatchmakingEngine extends HttpsPostHandler
  {
    @Override
    public String handleRequestAndGetResponse(String request)
    {
      long start = System.currentTimeMillis();
      while (MatchmakingEngineSingleton.get().getEventQueueSize() != 0 || MatchmakingEngineSingleton.get().getNumberOfPlayersInQueue() != 0
          || MatchHandlerSingleton.get().getEventQueueSize() != 0 || MatchHandlerSingleton.get().getState().equals(ProcessState.RUNNING))
      {
        try
        {
          Thread.sleep(10);
          if (System.currentTimeMillis() - start > 1000)
          {
            return GsonSingleton.toJson(new Response(false));
          }
        }
        catch (InterruptedException e)
        {
        }
      }
      return GsonSingleton.toJson(new Response(true));
    }
  }
}
