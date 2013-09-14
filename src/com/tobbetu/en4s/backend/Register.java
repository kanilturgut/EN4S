package com.tobbetu.en4s.backend;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Register {

    private final String registerInfo;

    public Register(String email, String first_name, String last_name,
            String password) {
        JSONObject register = new JSONObject();

        try {
            register.put("email", email);
            register.put("first_name", first_name);
            register.put("last_name", last_name);
            register.put("password", password);

        } catch (JSONException e) {
            Log.e("Register", "wierd JSONException", e);
        }

        this.registerInfo = register.toString();
    }

    public void register() throws IOException, RegisterFailedException,
            JSONException {
        HttpResponse loginResponse = Requests.post("/register", registerInfo);
        if (Requests.checkStatusCode(loginResponse, HttpStatus.SC_NOT_FOUND))
            throw new RegisterFailedException();
        if (Requests.checkStatusCode(loginResponse, HttpStatus.SC_CREATED)) {
            String response = Requests.readResponse(loginResponse);
            Login.setMe(User.fromJSON(response));
        } else {
            // Don't know what happened but whatever happened, it must be very
            // very bad
            throw new RegisterFailedException();
        }
    }

    public class RegisterFailedException extends Exception {

        private static final long serialVersionUID = -4479773881373169658L;

    }
}
