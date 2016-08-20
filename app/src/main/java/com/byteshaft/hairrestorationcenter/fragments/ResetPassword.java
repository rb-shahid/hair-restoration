package com.byteshaft.hairrestorationcenter.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.hairrestorationcenter.MainActivity;
import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.Helpers;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ResetPassword extends Fragment {

    private View mBaseView;
    private EditText mNewPassword;
    private EditText mOldPassword;
    private EditText mEmail;
    private Button mResetButton;

    private String mEmailAddressString;
    private String mOldPasswordString;
    private String mNewPasswordrString;
    private String mPasswordString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.reset_password, container, false);
        setHasOptionsMenu(true);
        mEmail = (EditText) mBaseView.findViewById(R.id.email_address);
        mEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        mEmail.setEnabled(false);
        mOldPassword = (EditText) mBaseView.findViewById(R.id.old_password);
        mNewPassword = (EditText) mBaseView.findViewById(R.id.password);
        mResetButton = (Button) mBaseView.findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEditText()) {
                    if (AppGlobals.sIsInternetAvailable) {
                        new ResetPasswordTask(false).execute();
                    } else {
                        Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                                executeTask(true));
                    }
                }

            }
        });
        return mBaseView;
    }

    private Runnable executeTask(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new ResetPasswordTask(value).execute();
            }
        };
        return runnable;
    }

    private boolean validateEditText() {

        boolean valid = true;
        mPasswordString = mNewPassword.getText().toString();
        mOldPasswordString = mOldPassword.getText().toString();
        mEmailAddressString = mEmail.getText().toString();


        if (mPasswordString.trim().isEmpty() || mPasswordString.length() < 3) {
            mNewPassword.setError("enter at least 3 characters");
            valid = false;
        } else {
            mNewPassword.setError(null);
        }

        if (mOldPasswordString.trim().isEmpty() || mOldPasswordString.length() < 3) {
            mOldPassword.setError("enter at least 3 characters");
            valid = false;
        } else {
            mOldPassword.setError(null);
        }

        if (mEmailAddressString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailAddressString).matches()) {
            mEmail.setError("please provide a valid email");
            valid = false;
        } else {
            mEmail.setError(null);
        }
        return valid;
    }

    class ResetPasswordTask extends AsyncTask<String, String, String> {

        public ResetPasswordTask(boolean checkInternet) {
            this.checkInternet = checkInternet;
        }

        private boolean checkInternet = false;
        private JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelpers.showProgressDialog(getActivity(), "Resetting your password");
        }

        @Override
        protected String doInBackground(String... strings) {

            if (AppGlobals.sIsInternetAvailable) {
                sendData();
            } else if (checkInternet){
                if (WebServiceHelpers.isNetworkAvailable()) {
                    sendData();
                }
            }
            return null;
        }

        private void sendData() {
            try {
                jsonObject = WebServiceHelpers.resetPassword(
                        mEmailAddressString,
                        mOldPasswordString,
                        mPasswordString);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            WebServiceHelpers.dismissProgressDialog();
            Log.i("TAG", jsonObject.toString());
            try {
                if (jsonObject.getString("Message").equals("Input is invalid") || jsonObject.get("Message").equals("Old Password Wrong")) {
                    AppGlobals.alertDialog(getActivity(), "Resetting Failed!", "old Password is wrong");
                } else if (jsonObject.getString("Message").equals("Successfully")) {
                    System.out.println(jsonObject + "working");
                    MainActivity.loadFragment(new EducationFragment());
                    Toast.makeText(getActivity(), "Your password successfully changed", Toast.LENGTH_SHORT).show();
                } else {
                    Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                            executeTask(true));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
