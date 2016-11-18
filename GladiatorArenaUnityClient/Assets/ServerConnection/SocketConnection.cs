using UnityEngine;
using System.Collections;
using SimpleJSON;

public class SocketConnection : Process
{
    public const int ALIVE_CHECK_INTERVAL_MS = 1000;
    private NetworkChannel channel;

    public SocketConnection(string ip, int port)
    {
        this.channel = new NetworkChannel(ip, port);
        this.channel.SetNotifier(this);
        this.channel.Start();
    }

    public string GetMessage()
    {
        ProcessEvent ev = this.PeekEvent();
        NetworkPacket np = ev as NetworkPacket;
        if (np != null)
        {
            return np.GetMessage();
        }
        return null;
    }

    public bool SendMessage(string msg)
    {
        return this.channel.SendMessage(msg);
    }

    public override void Life()
    {
        while(this.stayAlive)
        {
            var json = new JSONClass();
            json["method"] = "Ping";
            this.SendMessage(json.ToString());
            System.Threading.Thread.Sleep(ALIVE_CHECK_INTERVAL_MS);
        }
    }

    public bool IsAlive()
    {
        return this.channel.GetState() != Process.FINISHED;
    }

    public override void CloseSpecific()
    {
        this.channel.Close();
        this.channel.WaitUntillDone();
    }
}
