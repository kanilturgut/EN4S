package com.tobbetu.en4s.backend;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public abstract class Login {

    private final String loginInfo;
    private final String url;

    public Login(String url, String arg0name, String arg0, String arg1name,
            String arg1) {
        this.url = url;
        JSONObject login = new JSONObject();
        try {
            login.put(arg0name, arg0);
            login.put(arg1name, arg1);
        } catch (JSONException e) {
            Log.e(getClass().getName(), "JSONException", e);
        }
        this.loginInfo = login.toString();
    }

    public void makeRequest() throws IOException, LoginFailedException {
        HttpResponse loginResponse = Requests.post(this.url, loginInfo);
        if (Requests.checkStatusCode(loginResponse, HttpStatus.SC_NOT_FOUND))
            throw new LoginFailedException();
    }

    public class LoginFailedException extends Exception {

        private static final long serialVersionUID = 2996222505192491564L;

    }
}
