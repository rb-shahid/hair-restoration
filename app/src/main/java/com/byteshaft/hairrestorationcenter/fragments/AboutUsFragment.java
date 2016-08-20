package com.byteshaft.hairrestorationcenter.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.Helpers;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AboutUsFragment extends Fragment {

    private View mBaseView;
    private TextView mAboutUsTextView;
    private String aboutUs;
    private boolean foreground = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.aboutus_fragment, container, false);
        setHasOptionsMenu(true);
        mAboutUsTextView = (TextView) mBaseView.findViewById(R.id.textview_about_us);
        if (AppGlobals.sIsInternetAvailable) {
            new AboutUsTask(false).execute();
        } else {
            Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                    executeTask(true));
        }

        return mBaseView;
    }

    private Runnable executeTask(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new AboutUsTask(value).execute();
            }
        };
        return runnable;
    }

    @Override
    public void onResume() {
        super.onResume();
        foreground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }

    private class AboutUsTask extends AsyncTask<String, String, String> {

        private boolean checkInternet = false;

        public AboutUsTask(boolean checkInternet) {
            this.checkInternet = checkInternet;
        }

        private JSONObject jsonObject;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Helpers.getProgressDialog(getActivity());
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            if (AppGlobals.sIsInternetAvailable){
                sendRequest();
            } else if (checkInternet) {
                if (WebServiceHelpers.isNetworkAvailable()) {
                    sendRequest();
                }
            }
            return aboutUs;
        }

        private void sendRequest() {
            try {
                jsonObject = WebServiceHelpers.aboutUs();
                if (jsonObject.getString("Message").equals("Successfully")) {
                    JSONObject data = jsonObject.getJSONObject("details");
                    aboutUs = data.getString("aboutus");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (foreground) {
                progressDialog.dismiss();
                if (s != null) {
                    mAboutUsTextView.setText(Html.fromHtml(s));
                } else {
                    Helpers.alertDialog(getActivity(), "No internet", "Please check your internet " +
                            "connection", executeTask(true));
                }
            }
        }
    }
}
