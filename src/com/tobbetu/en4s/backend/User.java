package com.tobbetu.en4s.backend;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {

    private static final long serialVersionUID = -8853898204431440970L;

    private String id;
    private String email;
    private String name;

    public User() {
    }

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public static User fromJSON(String response) throws JSONException {
        return User.fromJSON(new JSONObject(response));
    }

    public static User fromJSON(JSONObject obj) throws JSONException {
        return new User(obj.getString("_id"), obj.getString("email"),
                obj.getString("name"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
