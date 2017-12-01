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
import com.github.malow.malowlib.network.https.SimpleHttpsServer;
import com.github.malow.malowlib.network.https.SimpleHttpsServerConfig;
import com.github.malow.malowlib.network.https.SimpleHttpsServerConfig.LetsEncryptConfig;

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
    SimpleHttpsServerConfig httpsConfig = new SimpleHttpsServerConfig(7000, new LetsEncryptConfig("LetsEncryptCerts"), "password");
    GladiatorArenaServerConfig gladConfig = new GladiatorArenaServerConfig(7001);

    AccountServerConfig accountServerConfig = new AccountServerConfig(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"),
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");
    // DEFAULT EMAIL-SENDING TO FALSE FOR STAGING
    accountServerConfig.enableEmailSending = false;

    this.start(gladConfig, accountServerConfig, httpsConfig);
  }

  private ClientSocketListener socketListener;
  private SimpleHttpsServer httpsServer;

  public void start(GladiatorArenaServerConfig gladConfig, AccountServerConfig accountServerConfig, SimpleHttpsServerConfig httpsServerConfig)
  {
    this.httpsServer = new SimpleHttpsServer(httpsServerConfig);
    this.httpsServer.start();
    MaloWLogger.info("Starting GladiatorArenaServer in directory " + System.getProperty("user.dir") + " using port " + this.httpsServer.getPort()
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

    AccountServer.start(accountServerConfig, this.httpsServer);

    this.httpsServer.createContext("/createuser", new CreateUserHandler());
    this.httpsServer.createContext("/getmyinfo", new GetMyInfoHandler());
    this.httpsServer.createContext("/queuematchmaking", new QueueMatchmakingHandler());
    this.httpsServer.createContext("/unqueuematchmaking", new UnqueueMatchmakingHandler());
    if (gladConfig.allowTestOperations)
    {
      this.httpsServer.createContext("/clearcache", new ClearCacheHandler());
      this.httpsServer.createContext("/waitforemptymatchmakingengine", new WaitForEmptyMatchmakingEngine());
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
    //DatabaseConnection.closeAll();
    this.httpsServer.close();
  }

  @Command(description = "Creates the databases and tables used. Deletes and drops any preexisting.")
  public void createDatabases() throws Exception
  {
    MatchReferenceAccessorSingleton.get().createTable();
    UserAccessorSingleton.get().createTable();
    MatchAccessorSingleton.get().createTable();
    AccountServer.createDatabases();
  }

  @Command(description = "Enables sending emails.")
  public void enableEmails() throws Exception
  {
    AccountServer.enableEmailSending();
  }

  @Command(description = "Disables sending emails.")
  public void disableEmails() throws Exception
  {
    AccountServer.disableEmailSending();
  }
}
