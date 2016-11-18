using UnityEngine;
using System.Collections;
using SimpleJSON;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class MainMenuScript : MonoBehaviour {

    public GuiElementScript loginButton;
    public GuiElementScript registerButton;
    public GuiElementScript resetPWButton;

    public GuiElementScript emailInput;
    public GuiElementScript passwordInput;
    public GuiElementScript usernameInput;
    public GuiElementScript resetPwTokenInput;
    public GuiElementScript continueButton;
    public GuiElementScript backButton;

    public const int STATE_NONE = 0;
    public const int STATE_LOGIN = 1;
    public const int STATE_REGISTER = 2;
    public const int STATE_SEND_PW_TOKEN = 3;
    public const int STATE_RESET_PW = 4;
    private int state = STATE_NONE;


    // Use this for initialization
    void Start () {
        HttpsClient.base_url = "https://malow.duckdns.org:7000";
        this.goToFirstStep();
	}
	
	// Update is called once per frame
	void Update () {
	
	}

    public void goToLogin()
    {
        this.state = STATE_LOGIN;
        this.goToSecondStep();
        this.showLoginGUI();
    }

    public void goToRegister()
    {
        this.state = STATE_REGISTER;
        this.goToSecondStep();
        this.showRegisterGUI();
    }

    public void goToSendPwToken()
    {
        this.state = STATE_SEND_PW_TOKEN;
        this.goToSecondStep();
        this.showSendPwResetGUI();
    }

    public void goContinue()
    {
        if(this.state == STATE_LOGIN)
        {
            this.doLogin();
        }
        else if (this.state == STATE_REGISTER)
        {
            this.doRegister();
        }
        else if (this.state == STATE_SEND_PW_TOKEN)
        {
            this.doSendPasswordResetToken();
        }
        else if (this.state == STATE_RESET_PW)
        {
            this.doResetPassword();
        }
    }

    public void goBack()
    {
        this.state = STATE_NONE;
        this.goToFirstStep();
    }

    private void goToSecondStep()
    {
        this.loginButton.gameObject.SetActive(false);
        this.registerButton.gameObject.SetActive(false);
        this.resetPWButton.gameObject.SetActive(false);
        this.continueButton.gameObject.SetActive(true);
        this.backButton.gameObject.SetActive(true);
    }

    private void goToFirstStep()
    {
        this.loginButton.gameObject.SetActive(true);
        this.registerButton.gameObject.SetActive(true);
        this.resetPWButton.gameObject.SetActive(true);

        this.emailInput.gameObject.SetActive(false);
        this.usernameInput.gameObject.SetActive(false);
        this.passwordInput.gameObject.SetActive(false);
        this.resetPwTokenInput.gameObject.SetActive(false);
        this.continueButton.gameObject.SetActive(false);
        this.backButton.gameObject.SetActive(false);
    }

    private void showLoginGUI()
    {
        this.emailInput.gameObject.SetActive(true);
        this.passwordInput.gameObject.SetActive(true);
    }

    private void showRegisterGUI()
    {
        this.emailInput.gameObject.SetActive(true);
        this.passwordInput.gameObject.SetActive(true);
        this.usernameInput.gameObject.SetActive(true);
    }

    private void showSendPwResetGUI()
    {
        this.emailInput.gameObject.SetActive(true);
    }

    private void showResetPwGUI()
    {
        this.state = STATE_RESET_PW;
        this.emailInput.gameObject.SetActive(false);
        this.resetPwTokenInput.gameObject.SetActive(true);
        this.passwordInput.gameObject.SetActive(true);
    }

    private void doLogin()
    {
        string email = this.emailInput.GetComponentInChildren<InputField>().text;
        string password = this.passwordInput.GetComponentInChildren<InputField>().text;
        JSONNode response = HttpsClient.login(email, password);
        if (response["result"].AsBool.Equals(true))
        {
            Debug.Log(response["authToken"] + " do something with it!");
            SceneManager.LoadScene("GameMenu");
        }
        else
        {
            Debug.Log(response["error"]);
        }
    }

    private void doRegister()
    {
        string username = this.emailInput.GetComponentInChildren<InputField>().text;
        string email = this.emailInput.GetComponentInChildren<InputField>().text;
        string password = this.passwordInput.GetComponentInChildren<InputField>().text;
        JSONNode response = HttpsClient.register(email, username, password);
        if (response["result"].AsBool.Equals(true))
        {
            Debug.Log(response["authToken"] + " do something with it!");
            SceneManager.LoadScene("GameMenu");
        }
        else
        {
            Debug.Log(response["error"]);
        }
    }

    private void doSendPasswordResetToken()
    {
        string email = this.emailInput.GetComponentInChildren<InputField>().text;
        JSONNode response = HttpsClient.sendPasswordResetToken(email);
        if (response["result"].AsBool.Equals(true))
        {
            this.showResetPwGUI();
        }
        else
        {
            Debug.Log(response["error"]);
        }
    }

    private void doResetPassword()
    {
        string email = this.emailInput.GetComponentInChildren<InputField>().text;
        string password = this.passwordInput.GetComponentInChildren<InputField>().text;
        string token = this.resetPwTokenInput.GetComponentInChildren<InputField>().text;
        JSONNode response = HttpsClient.resetPassword(email, password, token);
        if (response["result"].AsBool.Equals(true))
        {
            Debug.Log(response["authToken"] + " do something with it!");
            SceneManager.LoadScene("GameMenu");
        }
        else
        {
            Debug.Log(response["error"]);
        }
    }
}
