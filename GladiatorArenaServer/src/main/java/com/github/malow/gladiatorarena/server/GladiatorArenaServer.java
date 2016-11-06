package com.github.malow.gladiatorarena.server;

import java.util.Scanner;

import com.github.malow.accountserver.AccountServer;
import com.github.malow.accountserver.AccountServerConfig;
import com.github.malow.gladiatorarena.server.game.socketnetwork.SocketListener;
import com.github.malow.gladiatorarena.server.handlers.GameInstanceHandler;
import com.github.malow.gladiatorarena.server.handlers.HttpsHandlers.GetMyInfoHandler;
import com.github.malow.gladiatorarena.server.handlers.HttpsHandlers.QueueMatchmakingHandler;
import com.github.malow.malowlib.network.https.HttpsPostServer;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig;

public class GladiatorArenaServer
{
  public static void main(String[] args)
  {
    // Setup SocketListener and GameInstanceHandler
    GameInstanceHandler.getInstance().start();
    SocketListener socketListener = new SocketListener(8000);
    socketListener.start();

    // Setup AccountServer
    HttpsPostServerConfig accountServerHttpsConfig = new HttpsPostServerConfig(7000, "https_key.jks", "password");
    AccountServerConfig accountServerConfig = new AccountServerConfig("GladiatorArenaServer", "GladArUsr", "password", accountServerHttpsConfig,
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");
    accountServerConfig.enableEmailSending = false;
    AccountServer.start(accountServerConfig);

    // Setup GameHttpsServer
    HttpsPostServerConfig gameConfig = new HttpsPostServerConfig(7001, "https_key.jks", "password");
    HttpsPostServer gameHttpsServer = new HttpsPostServer(gameConfig);
    gameHttpsServer.createContext("/getmyinfo", new GetMyInfoHandler());
    gameHttpsServer.createContext("/queuematchmaking", new QueueMatchmakingHandler());
    gameHttpsServer.start();

    String input = "";
    Scanner in = new Scanner(System.in);
    while (!input.equals("exit"))
    {
      System.out.print("> ");
      input = in.next();
    }
    in.close();

    AccountServer.close();
    gameHttpsServer.close();
    socketListener.close();
    socketListener.waitUntillDone();
    GameInstanceHandler.getInstance().close();
    GameInstanceHandler.getInstance().waitUntillDone();
  }
}
