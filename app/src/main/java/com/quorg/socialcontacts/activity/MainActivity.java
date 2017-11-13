package com.quorg.socialcontacts.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.quorg.socialcontacts.R;
import com.quorg.socialcontacts.adapter.ContactsAdapter;
import com.quorg.socialcontacts.database.SConnections;
import com.quorg.socialcontacts.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "NITISH";

    RecyclerView contactsRcView;
    ContactsAdapter contactsAdapter;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private boolean isLoading = false;
    public static final int PAGE_SIZE = 20;
    private LinearLayoutManager layoutManager;
    List<SConnections> sConnectionsList = new ArrayList<>();
    Toolbar toolbar;
    ImageView imgBack;
    TextView txtTitle;
    ImageView imgLogout;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseToolbar();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        contactsRcView = (RecyclerView) findViewById(R.id.rcvContacts);
        contactsAdapter = new ContactsAdapter(MainActivity.this, sConnectionsList);
        SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(10f));
        contactsRcView.setItemAnimator(animator);
        contactsRcView.setAdapter(contactsAdapter);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        contactsRcView.setLayoutManager(layoutManager);
        contactsRcView.addOnScrollListener(recyclerViewOnScrollListener);
        getDataFromDB(currentPage);

    }

    private void initialiseToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        imgBack = (ImageView) toolbar.findViewById(R.id.img_back);
        imgBack.setVisibility(View.VISIBLE);
        imgBack.setOnClickListener(this);
        imgLogout = (ImageView) toolbar.findViewById(R.id.img_options);
        imgLogout.setImageResource(R.drawable.ic_action_logout);
        imgLogout.setOnClickListener(this);
        txtTitle = (TextView) toolbar.findViewById(R.id.tv_cr_title);
        txtTitle.setText(R.string.app_name);
    }

    private void getDataFromDB(final int currentPage) {

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                int startIndex = currentPage;
                int endIndex = currentPage + 20;
                RealmResults<SConnections> realmResults = realm.where(SConnections.class)
                        .greaterThanOrEqualTo("id", startIndex)
                        .lessThan("id", endIndex).findAll();

                if (realmResults != null && realmResults.size() > 0) {
                    for (int i = 0; i < realmResults.size(); i++) {
                        SConnections sConnections = new SConnections();
                        sConnections.setName(realmResults.get(i).getName());
                        sConnections.setPhoneNumber(realmResults.get(i).getPhoneNumber());
                        sConnections.setEmail(realmResults.get(i).getEmail());
                        sConnectionsList.add(sConnections);
                    }
                }
                contactsAdapter.notifyDataSetChanged();
                isLoading = false;
                if (sConnectionsList.size() <= 500) {
                    isLastPage = false;
                } else {
                    isLastPage = true;
                }


            }
        });

    }


    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreContacts();
                }
            }
        }
    };


    private void loadMoreContacts() {

        isLoading = true;
        currentPage += 20;
        new GetContactsAsync().execute(currentPage);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
        sConnectionsList.clear();
        currentPage = 0;
    }

    private void removeListeners() {
        contactsRcView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.img_options:

                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Log.e("NITISH", "onResult: logged out finally ");
                            }
                        }
                );
                PreferenceUtils.setUserLoggedIn(MainActivity.this, false);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    class GetContactsAsync extends AsyncTask<Integer, Void, RealmResults<SConnections>> {
        ProgressDialog mProgressDialog;

        @Override
        protected RealmResults<SConnections> doInBackground(Integer... integers) {
            isLoading = true;
            int startIndex = integers[0];
            int endIndex = startIndex + 20;
            Realm realm = Realm.getDefaultInstance();
            RealmResults<SConnections> realmResults = realm.where(SConnections.class)
                    .greaterThan("id", startIndex)
                    .lessThan("id", endIndex)
                    .findAll();


            if (realmResults != null && realmResults.size() > 0) {
                for (int i = 0; i < realmResults.size(); i++) {
                    SConnections sConnections = new SConnections();
                    sConnections.setName(realmResults.get(i).getName());
                    sConnections.setPhoneNumber(realmResults.get(i).getPhoneNumber());
                    sConnections.setEmail(realmResults.get(i).getEmail());
                    sConnectionsList.add(sConnections);
                }
            }


            return realmResults;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = true;
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Fetching Data ... ");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();

        }

        @Override
        protected void onPostExecute(RealmResults<SConnections> sConnectionses) {
            super.onPostExecute(sConnectionses);
            contactsAdapter.notifyDataSetChanged();
            mProgressDialog.cancel();
            isLoading = false;
            if (sConnectionsList.size() <= 500) {
                isLastPage = false;
            } else {
                isLastPage = true;
            }
        }


    }
}
