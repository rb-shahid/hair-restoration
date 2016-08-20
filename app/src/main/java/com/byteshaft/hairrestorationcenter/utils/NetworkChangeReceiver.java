package com.byteshaft.hairrestorationcenter.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (activeNetInfo != null) {
            new CheckInternet().execute();
//            Toast.makeText(context, "Active Network Type : "
//                    + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
        }
        if (mobNetInfo != null) {
            new CheckInternet().execute();
//            Toast.makeText(context, "Mobile Network Type : "
//                    + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
        }

    }


    public static class  CheckInternet extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean isInternetAvailable = false;
            if (WebServiceHelpers.isNetworkAvailable() && WebServiceHelpers.isInternetActuallyWorking()) {
                isInternetAvailable = true;
            }
            return isInternetAvailable;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                AppGlobals.sIsInternetAvailable = true;
                Log.i("Receiver", "Internet available");
            } else {
                AppGlobals.sIsInternetAvailable = false;
//                alertDialog("No internet", "Please check your internet connection");
                Log.e("Receiver", "Internet Not Available");
            }
        }
    }
}