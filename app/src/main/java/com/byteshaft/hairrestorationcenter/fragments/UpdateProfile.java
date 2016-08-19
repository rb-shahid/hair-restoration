package com.byteshaft.hairrestorationcenter.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class UpdateProfile extends Fragment {

    private View mBaseView;
    private Button mUpdateButton;
    private EditText mUsername;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmailAddress;
    private EditText mZipCode;
    private EditText mPhoneNumber;

    private String mUsernameString;
    private String mFirstNameString;
    private String mLastNameString;
    private String mEmailAddressString;
    private String mZipCodeString;
    private String mUserIdString = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID);
    private String mPhoneNumberString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.update_profile, container, false);
        setHasOptionsMenu(true);
        mUsername = (EditText) mBaseView.findViewById(R.id.user_name);
        mFirstName = (EditText) mBaseView.findViewById(R.id.first_name);
        mLastName = (EditText) mBaseView.findViewById(R.id.last_name);
        mEmailAddress = (EditText) mBaseView.findViewById(R.id.email);
        mZipCode = (EditText) mBaseView.findViewById(R.id.zip_code);
        mPhoneNumber = (EditText) mBaseView.findViewById(R.id.phone);
        mUpdateButton = (Button) mBaseView.findViewById(R.id.update_button);
        mUsername.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_NAME));
        mLastName.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LASTNAME));
        mFirstName.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRSTNAME));
        mEmailAddress.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        mZipCode.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_ZIP_CODE));
        mPhoneNumber.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER));
        mEmailAddress.setEnabled(false);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsernameString = mUsername.getText().toString();
                mFirstNameString = mFirstName.getText().toString();
                mLastNameString = mLastName.getText().toString();
                mZipCodeString = mZipCode.getText().toString();
                mEmailAddressString = mEmailAddress.getText().toString();
                mPhoneNumberString = mPhoneNumber.getText().toString();
                if (AppGlobals.sIsInternetAvailable) {
                    new UpdateUserProfileTask(false).execute();
                } else {
                    Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                            executeTask(true));
                }

            }
        });
        return mBaseView;
    }

    private Runnable executeTask(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new UpdateUserProfileTask(value).execute();
            }
        };
        return runnable;
    }

    class UpdateUserProfileTask extends AsyncTask<String, String, JSONObject> {

        private boolean checkInternet = false;
        private JSONObject jsonObject;

        public UpdateUserProfileTask(boolean checkInternet) {
            this.checkInternet = checkInternet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelpers.showProgressDialog(getActivity(), "Updating User Profile");
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            if (AppGlobals.sIsInternetAvailable) {
                sendData();
            } else if (checkInternet) {
                if (WebServiceHelpers.isNetworkAvailable() && WebServiceHelpers.isInternetWorking()) {
                    sendData();
                }
            }
            return jsonObject;
        }

        private void sendData() {
            try {
                jsonObject = WebServiceHelpers.updateUserProfile(
                        mFirstNameString,
                        mLastNameString,
                        mEmailAddressString,
                        mPhoneNumberString,
                        mUserIdString,
                        mUsernameString,
                        mZipCodeString);
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
                    if (jsonObject.getString("Message").equals("Input is invalid;")) {
                        AppGlobals.alertDialog(getActivity(), "Registration Failed!", "username or email already exits");

                    } else if (jsonObject.getString("Message").equals("Successfully")) {
                        JSONObject data = jsonObject.getJSONObject("details");
                        //saving values
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRSTNAME, mFirstNameString);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LASTNAME, mLastNameString);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, mEmailAddressString);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, mPhoneNumberString);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ZIP_CODE, mZipCodeString);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, mUserIdString);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_NAME, mUsernameString);
                        MainActivity.loadFragment(new EducationFragment());
                        Toast.makeText(getActivity(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
