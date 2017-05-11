package com.github.malow.gladiatorarena.server.handlers;

import com.github.malow.accountserver.comstructs.Response;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.malowprocess.MaloWProcess.ProcessState;
import com.github.malow.malowlib.network.https.HttpsJsonPostHandler;
import com.github.malow.malowlib.network.https.HttpsPostResponse;
import com.github.malow.malowlib.network.https.HttpsPostTestRequest;

public class TestHttpsHandlers
{
  public static class ClearCacheHandler extends HttpsJsonPostHandler<HttpsPostTestRequest>
  {
    @Override
    public HttpsPostResponse handleRequestAndGetResponse(HttpsPostTestRequest request)
    {
      UserAccessorSingleton.get().clearCache();
      MatchmakingEngineSingleton.get().clearQueue();
      MaloWLogger.info("GladiatorArenaServer cleared cache.");
      return new Response(true);
    }
  }

  public static class WaitForEmptyMatchmakingEngine extends HttpsJsonPostHandler<HttpsPostTestRequest>
  {
    @Override
    public HttpsPostResponse handleRequestAndGetResponse(HttpsPostTestRequest request)
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
            MaloWLogger.warning("GladiatorArenaServer WaitForEmptyMatchmakingEngine timed out");
            return new Response(false);
          }
        }
        catch (InterruptedException e)
        {
          MaloWLogger.error("WaitForEmptyMatchmakingEngine failed", e);
        }
      }
      MaloWLogger.info("GladiatorArenaServer WaitForEmptyMatchmakingEngine successful after " + (System.currentTimeMillis() - start) + "ms.");
      return new Response(true);
    }
  }
}
