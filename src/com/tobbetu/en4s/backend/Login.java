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
    private static User me = null;

    public static User getMe() {
        return me;
    }

    public static void setMe(User me) {
        if (Login.me == null)
            Login.me = me;
    }

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

    public User makeRequest() throws IOException, LoginFailedException,
            JSONException {
        HttpResponse loginResponse = Requests.post(this.url, loginInfo);
        if (Requests.checkStatusCode(loginResponse, HttpStatus.SC_NOT_FOUND))
            throw new LoginFailedException();
        if (Requests.checkStatusCode(loginResponse, HttpStatus.SC_OK)) {
            String response = Requests.readResponse(loginResponse);
            me = User.fromJSON(response);
            return me;
        } else {
            // Don't know what happened but whatever happened, it must be very
            // very bad
            throw new LoginFailedException();
        }
    }

    public class LoginFailedException extends Exception {

        private static final long serialVersionUID = 2996222505192491564L;

    }
}
