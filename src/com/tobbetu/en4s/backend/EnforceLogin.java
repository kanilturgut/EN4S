package com.tobbetu.en4s.backend;

public class EnforceLogin extends Login {

    public EnforceLogin(String username, String passwd) {
        super("http://en4s.msimav.net/login", "username", username, "password",
                passwd);
    }

}
