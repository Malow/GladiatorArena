package com.github.malow.gladiatorarena.server;

import java.util.Optional;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.AccountServerConfig;
import com.github.malow.gladiatorarena.server.database.MatchAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.MatchReferenceAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.gladiatorarena.server.game.socketnetwork.ClientSocketListener;
import com.github.malow.gladiatorarena.server.handlers.MatchHandlerSingleton;
import com.github.malow.gladiatorarena.server.handlers.MatchmakingEngineSingleton;
import com.github.malow.gladiatorarena.server.handlers.TestHttpsHandlers.ClearCacheHandler;
import com.github.malow.gladiatorarena.server.handlers.TestHttpsHandlers.WaitForEmptyMatchmakingEngine;
import com.github.malow.gladiatorarena.server.handlers.UserHttpsHandlers.CreateUserHandler;
import com.github.malow.gladiatorarena.server.handlers.UserHttpsHandlers.GetMyInfoHandler;
import com.github.malow.gladiatorarena.server.handlers.UserHttpsHandlers.QueueMatchmakingHandler;
import com.github.malow.gladiatorarena.server.handlers.UserHttpsHandlers.UnqueueMatchmakingHandler;
import com.github.malow.malowlib.MaloWLogger;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseConnection.DatabaseType;
import com.github.malow.malowlib.malowcliapplication.Command;
import com.github.malow.malowlib.malowcliapplication.MaloWCliApplication;
import com.github.malow.malowlib.matchmakingengine.MatchmakingEngineConfig;
import com.github.malow.malowlib.network.https.HttpsPostServer;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig.LetsEncryptConfig;

public class GladiatorArenaServer extends MaloWCliApplication
{
  public static void main(String[] args)
  {
    MaloWLogger.setLoggingThresholdToInfo();
    GladiatorArenaServer gladiatorArenaServer = new GladiatorArenaServer();
    gladiatorArenaServer.run();
  }

  @Override
  public void onStart()
  {
    HttpsPostServerConfig httpsConfig = new HttpsPostServerConfig(7000, new LetsEncryptConfig("LetsEncryptCerts"), "password");
    HttpsPostServer httpsServer = new HttpsPostServer(httpsConfig);
    httpsServer.start();

    GladiatorArenaServerConfig gladConfig = new GladiatorArenaServerConfig(7001);

    AccountServerConfig accountServerConfig = new AccountServerConfig(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"),
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");
    // DEFAULT EMAIL-SENDING TO FALSE FOR STAGING
    accountServerConfig.enableEmailSending = false;

    this.start(gladConfig, accountServerConfig, httpsServer);
  }

  private ClientSocketListener socketListener;

  public void start(GladiatorArenaServerConfig gladConfig, AccountServerConfig accountServerConfig, HttpsPostServer httpsServer)
  {
    MaloWLogger.info("Starting GladiatorArenaServer in directory " + System.getProperty("user.dir") + " using port " + httpsServer.getPort()
        + " for HTTPS traffic and port " + gladConfig.gameSocketServerPort + " for game-socket traffic.");
    MatchHandlerSingleton.get().start();
    MatchmakingEngineConfig matchmakingEngineConfig = new MatchmakingEngineConfig();
    matchmakingEngineConfig.maxRatingDifference = Optional.of(1000.0);
    matchmakingEngineConfig.matchFinderInterval = Optional.of(10);
    MatchmakingEngineSingleton.init(matchmakingEngineConfig, MatchHandlerSingleton.get());
    MatchmakingEngineSingleton.get().start();

    this.socketListener = new ClientSocketListener(gladConfig.gameSocketServerPort, MatchHandlerSingleton.get());
    this.socketListener.start();

    UserAccessorSingleton.init(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"));
    MatchAccessorSingleton.init(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"));
    MatchReferenceAccessorSingleton.init(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"));

    AccountServer.start(accountServerConfig, httpsServer);

    httpsServer.createContext("/createuser", new CreateUserHandler());
    httpsServer.createContext("/getmyinfo", new GetMyInfoHandler());
    httpsServer.createContext("/queuematchmaking", new QueueMatchmakingHandler());
    httpsServer.createContext("/unqueuematchmaking", new UnqueueMatchmakingHandler());
    if (gladConfig.allowTestOperations)
    {
      httpsServer.createContext("/clearcache", new ClearCacheHandler());
      httpsServer.createContext("/waitforemptymatchmakingengine", new WaitForEmptyMatchmakingEngine());
    }
  }

  @Override
  public void onStop()
  {
    AccountServer.close();
    this.socketListener.close();
    this.socketListener.waitUntillDone();
    MatchmakingEngineSingleton.get().close();
    MatchmakingEngineSingleton.get().waitUntillDone();
    MatchHandlerSingleton.get().close();
    MatchHandlerSingleton.get().waitUntillDone();
  }

  @Command
  public void createDatabases() throws Exception
  {
    MatchReferenceAccessorSingleton.get().createTable();
    UserAccessorSingleton.get().createTable();
    MatchAccessorSingleton.get().createTable();
    AccountServer.createDatabases();
  }

  @Command
  public void enableEmails() throws Exception
  {
    AccountServer.enableEmailSending();
  }

  @Command
  public void disableEmails() throws Exception
  {
    AccountServer.disableEmailSending();
  }
}
