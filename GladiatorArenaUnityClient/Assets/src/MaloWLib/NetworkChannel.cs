

using System;
using System.IO;
using System.Net.Sockets;
using System.Text;

public class NetworkChannel : Process
{
	private string ip;
	private int port;
	private TcpClient client;
	NetworkStream ns;
	
	private Process notifier = null;
	private static long nextCID = 0;
	private long id;

	public NetworkChannel (string ip, int port)
	{		
		this.id = NetworkChannel.nextCID;
		NetworkChannel.nextCID++;

		this.ip = ip;
		this.port = port;
		this.client = new TcpClient(this.ip, this.port);
		this.ns = client.GetStream();
		this.ns.ReadTimeout = 10;
	}

	public void SendMessage(string msg)
	{
		msg += (char)10;
		this.ns.Write (Encoding.ASCII.GetBytes (msg), 0, msg.Length);
		this.ns.Flush ();
	}

	public override void Life()
	{
		while (this.stayAlive) 
		{
			String msg = this.ReceiveData();
			if(msg != null && msg != "")
			{
				if(this.notifier != null && this.stayAlive)
				{
					NetworkPacket np = new NetworkPacket(msg, this.id);
					this.notifier.PutEvent(np);
				}
			}
		}
	}

	public string ReceiveData()
	{
		if (this.ns.DataAvailable) {
			byte[] data = new byte[1024];
			int recv = this.ns.Read (data, 0, data.Length);
			return Encoding.ASCII.GetString (data, 0, recv);
		} else {
			System.Threading.Thread.Sleep(1);
			return "";
		}
	}

	public void SetNotifier(Process notifier) 
	{ 
		this.notifier = notifier; 
	}

	public long GetChannelID() 
	{ 
		return this.id; 
	}

	public override void CloseSpecific()
	{
		if(this.client == null)
			return;

		this.ns.Close();
		this.client.Close();
	}
}


