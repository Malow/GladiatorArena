package com.github.malow.gladiatorarena.server;

import java.util.Scanner;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.AccountServerConfig;
import com.github.malow.gladiatorarena.server.game.socketnetwork.SocketListener;
import com.github.malow.gladiatorarena.server.handlers.HttpsHandlers.ClearCacheHandler;
import com.github.malow.gladiatorarena.server.handlers.HttpsHandlers.GetMyInfoHandler;
import com.github.malow.gladiatorarena.server.handlers.HttpsHandlers.QueueMatchmakingHandler;
import com.github.malow.gladiatorarena.server.handlers.HttpsHandlers.UnqueueMatchmakingHandler;
import com.github.malow.gladiatorarena.server.handlers.MatchHandler;
import com.github.malow.malowlib.network.https.HttpsPostServer;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig.LetsEncryptConfig;

public class GladiatorArenaServer
{
  private static SocketListener socketListener;
  private static HttpsPostServer gameHttpsServer;

  public static void main(String[] args)
  {
    GladiatorArenaServerConfig gladConfig = new GladiatorArenaServerConfig();

    HttpsPostServerConfig accountServerHttpsConfig = new HttpsPostServerConfig(7000, new LetsEncryptConfig("LetsEncryptCerts"), "password");
    AccountServerConfig accountServerConfig = new AccountServerConfig("GladiatorArenaServer", "GladArUsr", "password", accountServerHttpsConfig,
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");

    HttpsPostServerConfig gameConfig = new HttpsPostServerConfig(7001, new LetsEncryptConfig("LetsEncryptCerts"), "password");

    startServer(gladConfig, accountServerConfig, gameConfig);

    String input = "";
    Scanner in = new Scanner(System.in);
    while (!input.equals("exit"))
    {
      System.out.print("> ");
      input = in.next();
    }
    in.close();

    closeServer();
  }

  public static void startServer(GladiatorArenaServerConfig gladConfig, AccountServerConfig accountServerConfig, HttpsPostServerConfig gameConfig)
  {
    // Setup SocketListener and GameInstanceHandler
    MatchHandler.getInstance().start();
    socketListener = new SocketListener(7002);
    socketListener.start();

    // Start AccountServer
    AccountServer.start(accountServerConfig);

    // Start HttpsGameApiServer
    gameHttpsServer = new HttpsPostServer(gameConfig);
    gameHttpsServer.createContext("/getmyinfo", new GetMyInfoHandler());
    gameHttpsServer.createContext("/queuematchmaking", new QueueMatchmakingHandler());
    gameHttpsServer.createContext("/unqueuematchmaking", new UnqueueMatchmakingHandler());
    if (gladConfig.allowClearCacheOperation)
    {
      gameHttpsServer.createContext("/clearcache", new ClearCacheHandler());
    }
    gameHttpsServer.start();
  }

  public static void closeServer()
  {
    AccountServer.close();
    gameHttpsServer.close();
    socketListener.close();
    socketListener.waitUntillDone();
    MatchHandler.getInstance().close();
    MatchHandler.getInstance().waitUntillDone();
  }
}

/*
Client1 -> Server: ready -> Response: true
Client2 -> Server: ready -> Response: true
Server -> Clients: MapData(double array with ints to represent tiles) -> Response: true
Server -> Clients: GameState(List of mercenaries, their positions, current healths etc.) -> Response: true
Client1 -> Server: ActionMove(mercId, list<Coords> path) -> Response: true
Server -> Client2: UpdateMove(mercId, Coords newPos) -> Response: true
Client1 -> Server: ActionAttack(mercId, Coords tile) -> Response: true
Server -> Client2: UpdateAttack(mercId from, mercId target, int targetNewHealth) -> Response: true
Client1 -> Server: ReadyForNextTurn -> Response: true
Client2 -> Server: ReadyForNextTurn -> Response: true
Server -> Clients: GameState(List of mercenaries, their positions, current healths etc.) -> Response: true
*/