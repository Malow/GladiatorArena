package com.github.malow.gladiatorarena.server;

import java.util.Scanner;

import org.junit.Test;

import com.github.malow.accountserver.AccountServerConfig;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig.LetsEncryptConfig;

public class GladiatorArenaServerForTests
{
  @Test
  public void runForIntegrationTests()
  {
    GladiatorArenaServerConfig gladConfig = new GladiatorArenaServerConfig();
    gladConfig.allowClearCacheOperation = true;

    HttpsPostServerConfig accountServerHttpsConfig = new HttpsPostServerConfig(7000, new LetsEncryptConfig("LetsEncryptCerts"), "password");
    AccountServerConfig accountServerConfig = new AccountServerConfig("GladiatorArenaServer", "GladArUsr", "password", accountServerHttpsConfig,
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");
    accountServerConfig.enableEmailSending = false;
    accountServerConfig.allowClearCacheOperation = true;

    HttpsPostServerConfig gameConfig = new HttpsPostServerConfig(7001, new LetsEncryptConfig("LetsEncryptCerts"), "password");

    GladiatorArenaServer.startServer(gladConfig, accountServerConfig, gameConfig);

    String input = "";
    Scanner in = new Scanner(System.in);
    while (!input.equals("exit"))
    {
      System.out.print("> ");
      input = in.next();
    }
    in.close();

    GladiatorArenaServer.closeServer();
  }
}
