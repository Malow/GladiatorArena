using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System.Collections.Specialized;
using System.Text;
using System.IO;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using SimpleJSON;

public class HttpsClient : MonoBehaviour {

    public static string base_url;
    
    public static JSONNode register(string email, string username, string password)
    {
        var json = JSON.Parse("{}");
        json["email"] = email;
        json["username"] = username;
        json["password"] = password;
        
        return sendMessage("/register", json.ToString());
    }

    public static JSONNode login(string email, string password)
    {
        var json = JSON.Parse("{}");
        json["email"] = email;
        json["password"] = password;

        return sendMessage("/login", json.ToString());
    }

    public static JSONNode sendPasswordResetToken(string email)
    {
        var json = JSON.Parse("{}");
        json["email"] = email;

        return sendMessage("/sendpwresettoken", json.ToString());
    }

    public static JSONNode resetPassword(string email, string password, string pwResetToken)
    {
        var json = JSON.Parse("{}");
        json["email"] = email;
        json["password"] = password;
        json["pwResetToken"] = pwResetToken;
        
        return sendMessage("/resetpw", json.ToString());
    }

    private static JSONNode sendMessage(string path, string json)
    {
        ServicePointManager.ServerCertificateValidationCallback = TrustCertificate;
        HttpWebRequest request = (HttpWebRequest)WebRequest.Create(base_url + path);
        request.Method = "POST";
        using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            streamWriter.Write(json);
        var response = request.GetResponse();
        string result;
        using (var reader = new StreamReader(response.GetResponseStream()))
        {
            result = reader.ReadToEnd();
        }
        return JSON.Parse(result);
    }

    private static bool TrustCertificate(object sender, X509Certificate x509Certificate, X509Chain x509Chain, SslPolicyErrors sslPolicyErrors)
    {
        // all Certificates are accepted
        return true;
    }
}
