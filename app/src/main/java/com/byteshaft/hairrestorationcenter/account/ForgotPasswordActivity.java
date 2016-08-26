package com.byteshaft.hairrestorationcenter.account;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.Helpers;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by husnain on 8/8/16.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private Button mRecoverButton;
    private EditText mEmail;
    private String mEmailString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);
        mEmail = (EditText) findViewById(R.id.email_address);
        mRecoverButton = (Button) findViewById(R.id.recover);
        mRecoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    if (AppGlobals.sIsInternetAvailable) {
                        new ForgotPasswordTask(false).execute();
                    } else {
                        Helpers.alertDialog(ForgotPasswordActivity.this, "No internet", "Please check your internet connection",
                                executeTask(true));
                    }
                }

            }
        });
    }

    private Runnable executeTask(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new ForgotPasswordTask(value).execute();
            }
        };
        return runnable;
    }

    public boolean validate() {
        boolean valid = true;
        mEmailString = mEmail.getText().toString();

        if (mEmailString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.
                matcher(mEmailString).matches()) {
            mEmail.setError("enter a valid email address");
            valid = false;
        }
        return valid;
    }

    class ForgotPasswordTask extends AsyncTask<String, String, String> {

        public ForgotPasswordTask(boolean checkInternet) {
            this.checkInternet  = checkInternet;
        }

        private JSONObject jsonObject;
        private boolean checkInternet = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelpers.showProgressDialog(ForgotPasswordActivity.this, "Sending Recovery Mail");
        }

        @Override
        protected String doInBackground(String... strings) {

            if (AppGlobals.sIsInternetAvailable) {
                sendData();
            } else if (checkInternet) {
                if (WebServiceHelpers.isNetworkAvailable()) {
                    sendData();
                }
            }
            return null;
        }

        private void sendData() {
            try {
                System.out.println(jsonObject == null);
                jsonObject = WebServiceHelpers.forgotPassword(mEmailString);
                Log.e("JSON","JSon"+ String.valueOf(jsonObject));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            WebServiceHelpers.dismissProgressDialog();
            try {
                if (jsonObject.getString("Message").equals("Input is invalid")) {
                    AppGlobals.alertDialog(ForgotPasswordActivity.this, "Recovery Failed!", "User does not exist" );

                }else if (jsonObject.getString("Message").equals("Successfully")) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please check your mail for new password", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Helpers.alertDialog(ForgotPasswordActivity.this, "No internet", "Please check your internet connection",
                            executeTask(true));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
