package com.tobbetu.en4s.backend;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Login {

    JSONObject loginInfo;

    public Login(String username, String passwd) {
        this.loginInfo = new JSONObject();
        try {
            this.loginInfo.put("username", username);
            this.loginInfo.put("password", passwd);
        } catch (JSONException e) {
            Log.e(getClass().getName(), "JSONException", e);
        }
    }

    public void makeRequest() throws IOException, LoginFailedException {
        HttpResponse loginResponse = Requests.post(
                "http://en4s.msimav.net/login", loginInfo.toString());
        if (Requests.checkStatusCode(loginResponse, HttpStatus.SC_NOT_FOUND))
            throw new LoginFailedException();
    }

    public class LoginFailedException extends Exception {

        private static final long serialVersionUID = 2996222505192491564L;

    }
}
