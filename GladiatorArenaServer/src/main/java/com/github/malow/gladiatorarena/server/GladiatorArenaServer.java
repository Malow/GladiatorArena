package com.github.malow.gladiatorarena.server;

import java.util.Optional;
import java.util.Scanner;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.AccountServerConfig;
import com.github.malow.gladiatorarena.server.database.MatchAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.MatchReferenceAccessorSingleton;
import com.github.malow.gladiatorarena.server.database.UserAccessorSingleton;
import com.github.malow.gladiatorarena.server.game.socketnetwork.SocketListener;
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
import com.github.malow.malowlib.matchmakingengine.MatchmakingEngineConfig;
import com.github.malow.malowlib.network.https.HttpsPostServer;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig.LetsEncryptConfig;

public class GladiatorArenaServer
{
  public static void main(String[] args)
  {
    MaloWLogger.setLoggingThresholdToInfo();
    HttpsPostServerConfig httpsConfig = new HttpsPostServerConfig(7000, new LetsEncryptConfig("LetsEncryptCerts"), "password");
    HttpsPostServer httpsServer = new HttpsPostServer(httpsConfig);
    httpsServer.start();

    GladiatorArenaServerConfig gladConfig = new GladiatorArenaServerConfig(7001);

    AccountServerConfig accountServerConfig = new AccountServerConfig(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"),
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");
    // DEFAULT EMAIL-SENDING TO FALSE FOR STAGING
    accountServerConfig.enableEmailSending = false;

    start(gladConfig, accountServerConfig, httpsServer);

    String input = "";
    Scanner in = new Scanner(System.in);
    while (!input.equals("exit"))
    {
      System.out.print("> ");
      input = in.next();
      handleInput(input);
      System.out.println("Done");
    }
    in.close();

    close();
  }

  private static SocketListener socketListener;

  static void start(GladiatorArenaServerConfig gladConfig, AccountServerConfig accountServerConfig, HttpsPostServer httpsServer)
  {
    MaloWLogger.info("Starting GladiatorArenaServer in directory " + System.getProperty("user.dir") + " using port " + httpsServer.getPort()
        + " for HTTPS traffic and port " + gladConfig.gameSocketServerPort + " for game-socket traffic.");
    MatchHandlerSingleton.get().start();
    MatchmakingEngineConfig matchmakingEngineConfig = new MatchmakingEngineConfig();
    matchmakingEngineConfig.maxRatingDifference = Optional.of(1000.0);
    matchmakingEngineConfig.matchFinderInterval = Optional.of(10);
    MatchmakingEngineSingleton.init(matchmakingEngineConfig, MatchHandlerSingleton.get());
    MatchmakingEngineSingleton.get().start();

    socketListener = new SocketListener(gladConfig.gameSocketServerPort);
    socketListener.start();

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

  static void close()
  {
    AccountServer.close();
    socketListener.close();
    socketListener.waitUntillDone();
    MatchmakingEngineSingleton.get().close();
    MatchmakingEngineSingleton.get().waitUntillDone();
    MatchHandlerSingleton.get().close();
    MatchHandlerSingleton.get().waitUntillDone();
  }

  static void handleInput(String command)
  {
    try
    {
      if (command.equals("createDatabases"))
      {
        UserAccessorSingleton.get().createTable();
        MatchAccessorSingleton.get().createTable();
        MatchReferenceAccessorSingleton.get().createTable();
        AccountServer.createDatabases();
      }
      else if (command.equals("enableEmails"))
      {
        AccountServer.enableEmailSending();
      }
      else if (command.equals("disableEmails"))
      {
        AccountServer.disableEmailSending();
      }
      else
      {
        throw new Exception("Unsupported command");
      }
    }
    catch (Exception e)
    {
      MaloWLogger.error("Error while handling input: " + command + ". Error: ", e);
    }
  }
}
