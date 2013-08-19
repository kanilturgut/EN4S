package com.tobbetu.en4s.backend;

public class EnforceLogin extends Login {

    public EnforceLogin(String email, String passwd) {
        super("http://en4s.msimav.net/login", "email", email, "password",
                passwd);
    }

}
