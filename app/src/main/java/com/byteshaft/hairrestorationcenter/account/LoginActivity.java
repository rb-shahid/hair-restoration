package com.byteshaft.hairrestorationcenter.account;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.hairrestorationcenter.MainActivity;
import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.Helpers;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginButton;
    private Button mRegisterButton;
    private TextView mForgotPasswordTextView;
    private String mPasswordString;
    private String mEmailString;
    private static LoginActivity sInstance;

    public static LoginActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        sInstance = this;
        mEmail = (EditText) findViewById(R.id.email_address);
        mPassword = (EditText) findViewById(R.id.password);
        mLoginButton = (Button) findViewById(R.id.login);
        mRegisterButton = (Button) findViewById(R.id.register);
        mLoginButton.setOnClickListener(this);
        mRegisterButton.setOnClickListener(this);
        mForgotPasswordTextView = (TextView) findViewById(R.id.tv_forgot_password);
        mForgotPasswordTextView.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("TAG", "called");
        finish();
        MainActivity.getInstance().finish();
    }

    public boolean validate() {
        boolean valid = true;

        mEmailString = mEmail.getText().toString();
        mPasswordString = mPassword.getText().toString();

        if (mEmailString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.
                matcher(mEmailString).matches()) {
            mEmail.setError("enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (mPasswordString.isEmpty() || mPassword.length() < 4) {
            mPassword.setError("Enter minimum 4 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }
        return valid;
    }

    private Runnable executeTask(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new LogInTask(value).execute();
            }
        };
        return runnable;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                if (validate()) {
                    if (AppGlobals.sIsInternetAvailable) {
                        new LogInTask(false).execute();
                    } else {
                        Helpers.alertDialog(LoginActivity.this, "No internet", "Please check your internet connection",
                                executeTask(true));
                    }
                }
                break;
            case R.id.register:
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                break;
            case R.id.tv_forgot_password:
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));

        }
    }

    class LogInTask extends AsyncTask<String, String, JSONObject> {

        public LogInTask(boolean checkInternet) {
            this.checkInternet = checkInternet;
        }

        private boolean checkInternet = false;
        private JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelpers.showProgressDialog(LoginActivity.this, "Logging In");
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            if (AppGlobals.sIsInternetAvailable) {
                sendData();
            } else if (checkInternet) {
                if (WebServiceHelpers.isNetworkAvailable()) {
                    sendData();
                }
            }
            return jsonObject;
        }

        private void sendData() {
            try {
                jsonObject = WebServiceHelpers.logInUser(
                        mEmailString,
                        mPasswordString
                        );
                Log.e("TAG", String.valueOf(jsonObject));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            WebServiceHelpers.dismissProgressDialog();
            if (jsonObject != null) {
                try {
                    if (jsonObject.getString("Message").equals("Input is invalid") || jsonObject.get("Message")
                            .equals("The information you entered is incorrect")) {
                        AppGlobals.alertDialog(LoginActivity.this, "Login Failed!", "Invalid Email or Password");

                    } else if (jsonObject.getString("Message").equals("Successfully")) {
                        JSONObject details = jsonObject.getJSONObject("details");
                        System.out.println(jsonObject + "working");
                        String username = details.getString(AppGlobals.KEY_USER_NAME);
                        String userId = details.getString(AppGlobals.KEY_USER_ID);
                        String firstName = details.getString(AppGlobals.KEY_FIRSTNAME);
                        String lastName = details.getString(AppGlobals.KEY_LASTNAME);
                        String email = details.getString(AppGlobals.KEY_EMAIL);
                        String phoneNumber = details.getString(AppGlobals.KEY_PHONE_NUMBER);
                        String zipCode = details.getString(AppGlobals.KEY_ZIP_CODE);

                        //saving values
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRSTNAME, firstName);
                        Log.i("First name", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRSTNAME));
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LASTNAME, lastName);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, phoneNumber);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ZIP_CODE, zipCode);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_NAME, username);
                        Toast.makeText(LoginActivity.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                        AppGlobals.saveUserLogin(true);
                        finish();
                        if (AppGlobals.logout) {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            AppGlobals.logout = false;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (jsonObject == null && !AppGlobals.sIsInternetAvailable) {
                Helpers.alertDialog(LoginActivity.this, "No internet", "Please check your internet connection",
                        executeTask(true));
            } else {
                AppGlobals.alertDialog(LoginActivity.this, "Error", "Please try again!");
            }
        }
    }
}
