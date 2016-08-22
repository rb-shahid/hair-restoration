package com.byteshaft.hairrestorationcenter.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

public class AppGlobals extends Application {

    private static Context sContext;
    private static final String BASE_URL = "https://dynobranding.com/client/hairapp/api/";
    public static final String REGISTER_URL = String.format("%ssignup.php?", BASE_URL);
    public static final String LOGIN_URL =  String.format("%ssignin.php?", BASE_URL);
    public static final String RESET_PASSWORD_URL = String.format("%sreset_password.php?", BASE_URL);
    public static final String FORGET_PASSWORD_URL = String.format("%sforgotpassword.php?", BASE_URL);
    public static final String UPDATE_PROFILE_URL = String.format("%supdate_profile.php?", BASE_URL);
    public static final String CONTACT_US_URL = String.format("%scontactus.php?", BASE_URL);
    public static final String ABOUT_US_URL = String.format("%saboutus.php", BASE_URL);
    public static final String EDUCATION_URL = String.format("%seducation_list.php", BASE_URL);
    public static final String LOCATIONS_URL = String.format("%slocations_list.php", BASE_URL);
    public static final String CONSULTATION_STEP_ONE = String.format("%sconsultation_step1.php", BASE_URL);
    public static final String SEND_MESSAGE = String.format("%ssend-message.php?", BASE_URL);
    public static final String RECEIVE_MESSAGE = String.format("%suser-message.php?", BASE_URL);
    public static final String KEY_USER_TOKEN = "token";
    public static final String KEY_FIRSTNAME = "firstname";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME= "username";
    public static final String KEY_PHONE_NUMBER = "phone";
    public static final String KEY_ZIP_CODE= "zip_code";
    public static final String USER_ACTIVATION_KEY = "activation_key";
    public static final String KEY_USER_LOGIN = "user_login";
    public static int responseCode = 0;
    public static int readresponseCode = 0;
    public static final String KEY_USER_DETAILS = "user_details";
    private static SharedPreferences sPreferences;
    public static final String QUESTION_LIST = String.format("%squeations_list.php", BASE_URL);
    public static int sEntryId = 0;
    public static final String CONSULTATION_STEP_2 = String.format("%sconsultation_step2.php", BASE_URL);
    public static boolean sConsultationSuccess = false;
    public static boolean sIsInternetAvailable = false;
    public static Activity sActivity;
    public static boolean logout = false;
    public static final String GCM_STATE_KEY = "gcm_key_state";
    public static final String SEND_REG_KEY = String.format("%sdevice_register.php?", BASE_URL);

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setResponseCode(int code) {
        responseCode = code;
    }

    public static int getResponseCode() {
        return responseCode;
    }

    public static SharedPreferences getPreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(AppGlobals.getContext());
    }

    public static void saveDataToSharedPreferences(String key, String value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getStringFromSharedPreferences(String key) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(key, "");
    }

    public static void saveUserLogin(boolean value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(AppGlobals.KEY_USER_LOGIN, value).apply();
    }

    public static boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(AppGlobals.KEY_USER_LOGIN, false);
    }

    public static void alertDialog(Activity activity, String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(msg).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static SharedPreferences getPreferences() {
        return sPreferences;
    }

    public static void saveRegKeyState(boolean value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(AppGlobals.GCM_STATE_KEY, value).apply();
    }

    public static boolean isRegKeysaved() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(AppGlobals.GCM_STATE_KEY, false);
    }
}
