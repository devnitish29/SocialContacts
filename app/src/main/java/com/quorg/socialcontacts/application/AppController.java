package com.quorg.socialcontacts.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Nitish Singh on 15/4/17.
 */

public class AppController extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        initRealm();
    }



    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("social_contacts.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}

