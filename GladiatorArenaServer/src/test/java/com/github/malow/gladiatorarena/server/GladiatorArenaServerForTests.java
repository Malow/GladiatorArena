package com.github.malow.gladiatorarena.server;

import org.junit.Test;

import com.github.malow.accountserver.AccountServerConfig;
import com.github.malow.malowlib.database.DatabaseConnection;
import com.github.malow.malowlib.database.DatabaseConnection.DatabaseType;
import com.github.malow.malowlib.network.https.HttpsPostServer;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig;
import com.github.malow.malowlib.network.https.HttpsPostServerConfig.LetsEncryptConfig;

public class GladiatorArenaServerForTests
{
  @Test
  public void runForIntegrationTests()
  {
    //MaloWLogger.setLoggingThresholdToInfo(); // For debugging tests
    HttpsPostServerConfig httpsConfig = new HttpsPostServerConfig(7000, new LetsEncryptConfig("LetsEncryptCerts"), "password");
    HttpsPostServer httpsServer = new HttpsPostServer(httpsConfig);
    httpsServer.start();

    GladiatorArenaServerConfig gladConfig = new GladiatorArenaServerConfig(7001);
    gladConfig.allowTestOperations = true;

    AccountServerConfig accountServerConfig = new AccountServerConfig(DatabaseConnection.get(DatabaseType.SQLITE_FILE, "GladiatorArena"),
        "gladiatormanager.noreply", "passwordFU", "GladiatorArena");
    accountServerConfig.enableEmailSending = false;
    accountServerConfig.allowTestOperations = true;

    GladiatorArenaServer gladiatorArenaServer = new GladiatorArenaServer();
    gladiatorArenaServer.start(gladConfig, accountServerConfig, httpsServer);
    while (true)
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
