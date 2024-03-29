package com.vinaykhemlani.ilovezappos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vinaykhemlani.ilovezappos.API.APIResponse;
import com.vinaykhemlani.ilovezappos.API.SearchResult;
import com.vinaykhemlani.ilovezappos.API.ZapposAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity implements Callback<APIResponse> {

    ZapposAPIService apiService;
    ProgressDialog progressDialog;
    String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeSearchField();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ZapposAPIService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ZapposAPIService.class);

    }

    private void initializeSearchField() {

        EditText editText = (EditText) findViewById(R.id.search_field);
        if (editText != null) {
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        beginSearch(null);
                        return true;
                    }
                    return false;
                }
            });
        }

    }

    public void beginSearch(View v) {

        // If there is an active network connection, perform the search
        if (isNetworkAvailable()) {

            // Close the keyboard if it's open
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            EditText searchField = (EditText) findViewById(R.id.search_field);

            if (searchField != null) {

                searchQuery = searchField.getText().toString();

                // Note: Zappos' website does not reject an empty string so it is allowed here.
                Call<APIResponse> response = apiService.performSearch(searchQuery);
                response.enqueue(SearchActivity.this);

                progressDialog = ProgressDialog.show(this, "",
                        "Loading search results for \"" + searchQuery + "\"...", true);

            } else {

                errorFunction();
                System.err.println("searchField is null");

            }


        } else {    // Otherwise, notify the user to retry with an active connection.

            new AlertDialog.Builder(this)
                    .setMessage("You are currently not connected to the internet. " +
                            "Please connect to the internet and retry your search!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

        }

    }

    /* ---------- Activity Utility Functions ---------- */

    /**
     * Displays a Toast notification when there is a problem with the API response.
     */
    private void errorFunction() {
        Toast.makeText(this, R.string.search_activity_search_error, Toast.LENGTH_LONG).show();
    }

    /**
     * Checks to make sure there is a network connection before making GET
     * request to Zappos API.
     *
     * @return  boolean whether or not there an active network connection
     *
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* ---------- Callback Implementation Functions ---------- */

    /**
     * Callback fired by retrofit2 if response is successfully received.
     * If response is successful, move to next activity.
     * Else, notify user with errorFunction.
     *
     * @param call      Original call that was made to API
     * @param response  Response from API request
     */
    @Override
    public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {

        // Dismiss progress dialog
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (response.isSuccessful()) {

            SearchResult firstResult = response.body().getResults().get(0);

            Intent intent = new Intent(this, ProductActivity.class);
            intent.putExtra(ProductActivity.DATA_IDENTIFIER, firstResult);
            intent.putExtra(ProductActivity.SEARCH_TERM_IDENTIFER, searchQuery);
            startActivity(intent);

        } else {
            errorFunction();
        }

    }

    /**
     * Callback fired if API requests fails. Notifies user and recommends
     * trying again.
     *
     * @param call  Original call made to API
     * @param t     Error thrown by request
     */
    @Override
    public void onFailure(Call<APIResponse> call, Throwable t) {

        // Dismiss progress dialog
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        System.err.println("Failed.");
        t.printStackTrace();
        errorFunction();

    }

}
