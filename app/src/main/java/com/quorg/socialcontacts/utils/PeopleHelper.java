package com.quorg.socialcontacts.utils;

import android.content.Context;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.quorg.socialcontacts.R;

import java.io.IOException;

/**
 * Created by Nitish Singh on 16/4/17.
 */

public class PeopleHelper {

    private static final String APPLICATION_NAME = "Social Contacts";


    public static PeopleService setUp(Context context, String serverAuthCode) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                context.getString(R.string.clientID),
                context.getString(R.string.clientSecret),
                serverAuthCode,
                redirectUrl).execute();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(context.getString(R.string.clientID), context.getString(R.string.clientSecret))
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();

        credential.setFromTokenResponse(tokenResponse);

        return new PeopleService.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
