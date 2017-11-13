package com.quorg.socialcontacts.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.quorg.socialcontacts.R;
import com.quorg.socialcontacts.database.SConnections;
import com.quorg.socialcontacts.utils.PeopleHelper;
import com.quorg.socialcontacts.utils.PreferenceUtils;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    SignInButton signInButtonGoogle;
    GoogleApiClient mGoogleApiClient;
    final int RC_INTENT = 200;
    final int RC_API_CHECK = 100;

    static String AUTH_TOKEN = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButtonGoogle = (SignInButton) findViewById(R.id.btn_google);
        signInButtonGoogle.setOnClickListener(this);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // The serverClientId is an OAuth 2.0 web client ID
                .requestServerAuthCode(getString(R.string.clientID))
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN),
                        new Scope(PeopleServiceScopes.CONTACTS_READONLY),
                        new Scope(PeopleServiceScopes.USER_EMAILS_READ),
                        new Scope(PeopleServiceScopes.USERINFO_EMAIL),
                        new Scope(PeopleServiceScopes.USER_PHONENUMBERS_READ))
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_INTENT:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                if (result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    AUTH_TOKEN = acct.getServerAuthCode();

                    new ContactsAsync().execute(acct.getServerAuthCode());

                } else {

                    Log.d("NITISH", result.getStatus().toString() + "\nmsg: " + result.getStatus().getStatusMessage());
                }
                break;

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = mGoogleApiAvailability.getErrorDialog(this, connectionResult.getErrorCode(), RC_API_CHECK);
        dialog.show();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_google:
                getAuthToken();
                break;
        }

    }

    private void getContact() {
        PreferenceUtils.setUserLoggedIn(LoginActivity.this, true);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.e("NITISH", "onResult: logged out finally ");
                    }
                }
        );
        PreferenceUtils.setUserLoggedIn(LoginActivity.this, false);
        Toast toast = Toast.makeText(LoginActivity.this, "No Contacts found from this account!!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private void getAuthToken() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_INTENT);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private class ContactsAsync extends AsyncTask<String, Void, Integer> {

        ProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(String... strings) {

            int sum = 0;

            try {
                PeopleService peopleService = PeopleHelper.setUp(LoginActivity.this, strings[0]);

                ListConnectionsResponse response = peopleService.people().connections()
                        .list("people/me")
                        .setPageSize(500)
                        .setRequestMaskIncludeField("person.names,person.emailAddresses,person.phoneNumbers")
                        .execute();

                final List<Person> connections = response.getConnections();

                if (connections != null && connections.size() > 0) {
                    Realm realm = Realm.getDefaultInstance();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.delete(SConnections.class);
                            for (int i = 0; i < connections.size(); i++) {
                                if (isCancelled()) break;
                                SConnections sConnections = realm.createObject(SConnections.class);

                                sConnections.setId(i);
                                if (connections.get(i).getNames() != null) {

                                    sConnections.setName(connections.get(i).getNames().get(0).getDisplayName());

                                }

                                if (connections.get(i).getEmailAddresses() != null) {

                                    sConnections.setEmail(connections.get(i).getEmailAddresses().get(0).getValue());

                                }


                                if (connections.get(i).getPhoneNumbers() != null) {

                                    sConnections.setPhoneNumber(connections.get(i).getPhoneNumbers().get(0).getValue());

                                }
                            }
                        }
                    });

                    sum = (int) realm.where(SConnections.class).count();
                    realm.close();


                } else {

                    Log.e("NITISH ", "No connections found. ");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return sum;
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressDialog.cancel();
            if (integer > 0) {
                getContact();
            } else {

                showToast();

            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Login in ...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();


        }


    }


}
