package com.byteshaft.hairrestorationcenter.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.hairrestorationcenter.MainActivity;
import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.Helpers;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HealthInformation extends Fragment implements
        HttpRequest.OnReadyStateChangeListener, View.OnClickListener {

    private Spinner gender;
    private EditText age;
    private ProgressDialog mProgressDialog;
    private HttpRequest mRequest;
    private ArrayList<JSONObject> fieldData;
    private HashMap<Integer, Integer> serverIds;
    private HashMap<Integer, String> answersList;
//    private Button submitButton;
    private StringBuilder stringBuilder = new StringBuilder();
    private ArrayList<Integer> requiredFields;
    private int idForGender = 2;
    private static boolean sPostRequest = false;
    private View mBaseView;

    private List<String> checkBoxAnswer;
    private LinearLayout mLinearLayout;
    private LinearLayout editTextItems;
    private LinearLayout checkBoxLayout;
    private ArrayList<Integer> editTextIds;
    private Button submitButton;
    private List<EditText> allEditTexts = new ArrayList<>();
    private ArrayList<Integer> arrayList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mBaseView = inflater.inflate(R.layout.health_information, container, false);
        fieldData = new ArrayList<>();
        serverIds = new HashMap<>();
        answersList = new HashMap<>();
        requiredFields = new ArrayList<>();
        checkBoxAnswer = new ArrayList<>();
        editTextIds = new ArrayList<>();
        age = (EditText) mBaseView.findViewById(R.id.age);
        gender = (Spinner) mBaseView.findViewById(R.id.gender);
        mLinearLayout = (LinearLayout) mBaseView.findViewById(R.id.main_layout);
        editTextItems = (LinearLayout) mBaseView.findViewById(R.id.edit_text_layout);
        checkBoxLayout = (LinearLayout) mBaseView.findViewById(R.id.checkbox_layout);
        submitButton = (Button) mBaseView.findViewById(R.id.submit_answers);
        submitButton.setOnClickListener(this);
        mProgressDialog = Helpers.getProgressDialog(getActivity());
        if (AppGlobals.sIsInternetAvailable) {
            new CheckInternet(false).execute();
        } else {
            Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                    executeTask(true));
        }
        return mBaseView;
    }

    private void getFieldsDetails() {
        sPostRequest = false;
        mProgressDialog.show();
        mRequest = new HttpRequest(getActivity().getApplicationContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.open("GET", AppGlobals.QUESTION_LIST);
        mRequest.send();
    }

    private Runnable executeTask(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new CheckInternet(value).execute();
            }
        };
        return runnable;
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int i) {
        switch (i) {
            case HttpRequest.STATE_DONE:
                mProgressDialog.dismiss();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        mProgressDialog.dismiss();
                        if (sPostRequest) {
                            Log.i("TAG", mRequest.getResponseText());
                            try {
                                JSONObject jsonObject = new JSONObject(mRequest.getResponseText());
                                if (jsonObject.getString("Message").equals("Successfully")) {

                                    AlertDialog.Builder alertDialogBuilder =
                                            new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Success");
                                    alertDialogBuilder.setMessage("Your details have uploaded " +
                                            "successfully.")
                                            .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            AppGlobals.sConsultationSuccess = true;
                                            ConsultationFragment.sUploaded = false;
                                            dialog.dismiss();
                                            MainActivity.loadFragment(new EducationFragment());
                                        }
                                    });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            parseJsonAndSetUi(mRequest.getResponseText());
                        }
                }
        }

    }

    private void parseJsonAndSetUi(String data) {
        JSONObject jsonObject;
        ViewGroup root = null;
        try {
            jsonObject = new JSONObject(data);
            if (jsonObject.getString("Message").equals("Successfully")) {
                JSONArray jsonArray = jsonObject.getJSONArray("details");
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject json = jsonArray.getJSONObject(i);
                    Log.i("TAG", "Boolean " + json.getString("title").equals("Gender"));
                    String itemType = json.getString("field_type");
                    if (itemType.equals("checkbox")) {
                        serverIds.put(i, json.getInt("id"));
                            if (json.getInt("required") == 1) {
                                requiredFields.add(i);
                                Log.i("REQUIRED", "fields" + requiredFields);
                            }
                        JSONArray checkBoxes = json.optJSONArray("field_data");
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        root = (ViewGroup)inflater.inflate(R.layout.layout_checkbox,
                                checkBoxLayout, false);
                        LinearLayout linearLayout = (LinearLayout) root.findViewById(R.id.inflate_checkbox);
                        TextView title = (TextView) root.findViewById(R.id.checkbox_title);
                        int mandatory = json.getInt("required");
                        title.setText(
                                getFormattedTitle(json.getString("title"), mandatory), TextView.BufferType.SPANNABLE);
                        for (int j = 0; j < checkBoxes.length(); j++) {
                            CheckBox checkBox = new CheckBox(getActivity());
                            checkBox.setText((String) checkBoxes.opt(j));
                            checkBox.setTextColor(getResources().getColor(android.R.color.white));
                            checkBox.setButtonDrawable(getResources().getDrawable(
                                    R.drawable.checkbox_background));
                            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if (b) {
                                        Log.i("Checkbox", " " + compoundButton.getText().toString());
                                        checkBoxAnswer.add(compoundButton.getText().toString());
                                        if (checkBoxAnswer.size() > 0) {
                                            try {
                                                answersList.put(json
                                                        .getInt("id"), checkBoxAnswer.toString());
                                                Log.i("checked", String.valueOf(answersList));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        checkBoxAnswer.remove(compoundButton.getText().toString());
                                        if (checkBoxAnswer.size() < 1) {
                                            try {
                                                answersList.remove(json
                                                        .getInt("id"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Log.i("Unchecked", String.valueOf(checkBoxAnswer));
                                    }
                                }
                            });
                            linearLayout.addView(checkBox);
                        }
                        checkBoxLayout.addView(root);
                    }  else if (json.getString("title").equals("Age")) {
                        serverIds.put(i, json.getInt("id"));
                        editTextIds.add(i);
                        age.setId(json.getInt("id"));
                        allEditTexts.add(age);
                        if (json.getInt("required") == 1) {
                            if (!requiredFields.contains(i)) {
                                requiredFields.add(i);
                                Log.e("TAG", "added age");
                            }
                        }
                    } else if (itemType.equals("textbox")) {
                        serverIds.put(i, json.getInt("id"));
                        if (json.getInt("required") == 1) {
                            requiredFields.add(i);
                            Log.i("REQUIRED", "fields" + requiredFields);
                        }
                        ViewGroup editTextRoot;
                        LayoutInflater editTextInflater = getActivity().getLayoutInflater();
                        editTextRoot = (ViewGroup) editTextInflater.inflate(R.layout.edittext_item,
                                editTextItems , false);
                        TextView title = (TextView) editTextRoot.findViewById(R.id.field_title);
                        EditText editText = (EditText) editTextRoot.findViewById(R.id.field_answer);
                        editText.setId(i);
                        allEditTexts.add(editText);
                        int mandatory = json.getInt("required");
                        Log.i("TAG", "title "+ json.getString("title"));
                        title.setText(getFormattedTitle(json.getString("title"), mandatory),
                                TextView.BufferType.SPANNABLE);
                        editTextItems.addView(editTextRoot);

                    }
                    if (json.getString("title").equals("Gender")) {
                        Log.e("GENDER", " gender");
                        idForGender = json.getInt("id");
                    }
                    Log.e("required fields ", "test " + requiredFields);
                }
            } else {
                AppGlobals.alertDialog(getActivity(), "Not Found", "Nothing found");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SpannableStringBuilder getFormattedTitle(String text, int mandatory) {
        SpannableStringBuilder realText = new SpannableStringBuilder();
        if (mandatory == 1) {
            String asterisk = "* ";
            SpannableString mandatorySpannable = new SpannableString(asterisk);
            mandatorySpannable.setSpan(
                    new ForegroundColorSpan(Color.RED), 0, asterisk.length(), 0);
            realText.append(mandatorySpannable);
        }
        SpannableString whiteSpannable = new SpannableString(text);
        whiteSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), 0);
        realText.append(whiteSpannable);
        return realText;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_answers:
                mLinearLayout.requestFocus();
                Log.i("TAG", "" + submitButton.hasFocus());
                if (AppGlobals.sEntryId == 0) {
                    Toast.makeText(getActivity(), "Please try again process failed",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.loadFragment(new ConsultationFragment());
                } else {
                    boolean result = validateEditText();
                    Log.i("boolean", " " + result);
                    if (result) {
                        mProgressDialog.show();
                        if (AppGlobals.sIsInternetAvailable) {
                            new SendData(false).execute();
                        } else {
                            Helpers.alertDialog(getActivity(), "No internet", "Please check your " +
                                    "internet connection",
                                    executeSendData(true));
                        }
                    }
                }
                break;
        }
    }

    private boolean validateEditText() {
        stringBuilder = new StringBuilder();
        boolean value = false;
        Log.i("TAG", String.valueOf(requiredFields));
        for(int i=0; i < allEditTexts.size(); i++) {
            Log.i("TAG", allEditTexts.get(i).getText().toString());
            String text  = allEditTexts.get(i).getText().toString();
            if (text.trim().isEmpty()) {
                Toast.makeText(getActivity(), "All required fields must be filled",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (requiredFields.contains(i)) {
                Log.i("TAG", " "+ i + " " + text.trim().isEmpty());
                if (!text.trim().isEmpty()){
                    stringBuilder.append(String.format("data[%d]=%s&", serverIds.get(i), text));
                    value = true;
                }
            }
        }
        if (answersList.size() < 1) {
            Toast.makeText(getActivity(), "All required fields must be filled",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        printMap(answersList);
        stringBuilder.append(String.format("user_id=%s&", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID)));
        stringBuilder.append(String.format("entry_id=%s&", AppGlobals.sEntryId));
        stringBuilder.append(String.format("data[%d]=%s", idForGender, gender.getSelectedItem().toString()));
        Log.e("String", stringBuilder.toString());
        return value;
    }

    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            arrayList.add((Integer) pair.getKey());
            stringBuilder.append(String.format("data[%d]=%s&", pair.getKey(),
                    pair.getValue().toString().replace("[", "").replace("]", "")));
            if (it.hasNext()) {
                it.next();
            }
        }
    }

    private void sendConsultationData(String data) {
        sPostRequest = true;
        mRequest = new HttpRequest(getActivity().getApplicationContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.open("POST", AppGlobals.CONSULTATION_STEP_2);
        mRequest.send(data);
    }

    class CheckInternet extends AsyncTask<String, String, Boolean> {

        public CheckInternet(boolean checkInternet) {
            this.checkInternet = checkInternet;
        }

        private boolean checkInternet = false;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading ...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean isInternetAvailable = false;
            if (AppGlobals.sIsInternetAvailable) {
                isInternetAvailable = true;
            } else if (checkInternet) {
                if (WebServiceHelpers.isNetworkAvailable()) {
                    isInternetAvailable = true;
                }

            }
            return isInternetAvailable;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if (aBoolean) {
                getFieldsDetails();
            } else {
                Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                        executeTask(true));
            }
        }
    }


        class SendData extends AsyncTask<String, String, Boolean> {

            public SendData(boolean checkInternet) {
                this.checkInternet = checkInternet;
            }

            private boolean checkInternet = false;
            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Sending...");
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(String... strings) {
                boolean isInternetAvailable = false;
                if (AppGlobals.sIsInternetAvailable) {
                    isInternetAvailable = true;
                } else if (checkInternet) {
                    if (WebServiceHelpers.isNetworkAvailable()) {
                        isInternetAvailable = true;
                    }
                }

                return isInternetAvailable;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                progressDialog.dismiss();
                if (aBoolean) {
                    sendConsultationData(stringBuilder.toString());
                } else {
                    Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                            executeSendData(true));
                }
            }
        }

    private Runnable executeSendData(final boolean value) {
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                new SendData(value).execute();
            }
        };
        return runnable;
    }
    }
