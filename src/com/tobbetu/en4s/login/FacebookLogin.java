package com.tobbetu.en4s.login;


public class FacebookLogin extends Login {

    public FacebookLogin(String email, String accessToken, String regId) {
        super("/user/login/facebook", regId, "email", email, "access_token",
                accessToken);
    }

}
