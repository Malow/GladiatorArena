using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class MainScript : MonoBehaviour {
    public GuiElementScript gameTokenInput;

    // Use this for initialization
    void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {

    }

    public void joinGame()
    {
        string gameToken = this.gameTokenInput.GetComponentInChildren<InputField>().text;
        Debug.Log("Joining game: " + gameToken);
        SceneManager.LoadScene("game");
    }
}
