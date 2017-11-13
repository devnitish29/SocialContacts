package com.quorg.socialcontacts.database;

import io.realm.RealmObject;

/**
 * Created by Nitish Singh on 15/4/17.
 */

public class SConnections extends RealmObject {

    private int id;

    private String name;

    private String email;

    private String phoneNumber;


    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
