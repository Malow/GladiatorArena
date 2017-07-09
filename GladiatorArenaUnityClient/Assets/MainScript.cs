using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class MainScript : MonoBehaviour {
    public GuiElementScript gameTokenInput;

    private ServerConnection server = null;

    // Use this for initialization
    void Start () {
        this.server = new ServerConnection("malow.mooo.com", 7000);
        this.server.SendMessage(new LoginRequest("a", "a"));
    }
	
	// Update is called once per frame
	void Update () {
        ModelInterface msg = server.GetMessage();
        if (msg != null)
        {
            this.HandleServerMessage(msg);
        }
    }

    public void joinGame()
    {
        string gameToken = this.gameTokenInput.GetComponentInChildren<InputField>().text;
        Debug.Log("Joining game: " + gameToken);
        SceneManager.LoadScene("game");
    }

    private void HandleServerMessage(ModelInterface msg)
    {
        PositionUpdate pu = msg as PositionUpdate;
        if (pu != null)
        {
            return;
        }
    }
}
