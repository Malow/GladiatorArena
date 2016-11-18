using UnityEngine;
using System;
using SimpleJSON;

public class SocketClient
{
    private static string email;
    private static string authToken;
    private static string ip;
    private static int port;
    private static SocketConnection connection;
    private static bool hasBeenInitiated = false;

	public static void Init (string ip, int port, string email, string authToken)
    { 
        SocketClient.authToken = authToken;
        SocketClient.email = email;
        SocketClient.ip = ip;
        SocketClient.port = port;
        SocketClient.ConnectAndAuth();
        SocketClient.hasBeenInitiated = true;
    }

    public static string GetMessage()
    {
        string msg = SocketClient.connection.GetMessage();
        if (msg == null && !SocketClient.connection.IsAlive())
        {
            SocketClient.ConnectAndAuth();
        }
        return msg;
    }

    public static bool SendMessage(string message)
    {
        if (!SocketClient.connection.SendMessage(message))
        {
            SocketClient.ConnectAndAuth();
            return SocketClient.connection.SendMessage(message);
        }
        return true;
    }

    public static bool HasBeenInitiated()
    {
        return SocketClient.hasBeenInitiated;
    }

    private static void ConnectAndAuth()
    {
        if(SocketClient.connection != null)
        {
            SocketClient.connection.Close();
        }
        SocketClient.connection = new SocketConnection(ip, port);
        SocketClient.connection.Start();
        var json = JSON.Parse("{}");
        json["method"] = "Authentication";
        json["email"] = SocketClient.email;
        json["authToken"] = SocketClient.authToken;
        SocketClient.connection.SendMessage(json.ToString());
    }

    public static void HandleAuthenticationMessage(JSONNode message)
    {
        if (message["result"].AsBool.Equals(false))
        {
            Debug.Log("Authentication call failed");
        }
    }
}


