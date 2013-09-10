package com.tobbetu.en4s.backend;

public class FacebookLogin extends Login {

    public FacebookLogin(String email, String accessToken) {
        super("/login/facebook", "email", email, "access_token", accessToken);
    }

}
