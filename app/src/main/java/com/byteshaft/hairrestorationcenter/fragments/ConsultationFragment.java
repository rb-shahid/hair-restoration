package com.byteshaft.hairrestorationcenter.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.Helpers;
import com.byteshaft.hairrestorationcenter.utils.RotateUtil;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ConsultationFragment extends Fragment implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnFileUploadProgressListener {

    private View mBaseView;
    private CircularImageView mFrontSide;
    private CircularImageView mBackSide;
    private CircularImageView mTopSide;
    private CircularImageView mLeftSide;
    private CircularImageView mRightSide;
    private Button mUploadButton;
    private Intent intent;
    private String filePath;
    private Uri uriSavedImage;
    private HashMap<Integer, String> imagesHashMap;
    private final int[] requestCodes = {1, 2, 3, 4, 5};
    private int pressedButtonId;
    private HttpRequest mRequest;
    private ArrayList<String> uploaded;
    private TextView uploadDetails;
    private ProgressBar mProgressBar;
    private FrameLayout progressLayout;
    private TextView percentAge;
    private ProgressDialog progressDialog;
    public static boolean sUploaded = false;
    private boolean selectImage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.consultation_fragment, container, false);
        setHasOptionsMenu(true);
        imagesHashMap = new HashMap<>();
        uploaded = new ArrayList<>();
        mFrontSide = (CircularImageView) mBaseView.findViewById(R.id.front_side);
        mLeftSide = (CircularImageView) mBaseView.findViewById(R.id.left_side);
        mBackSide = (CircularImageView) mBaseView.findViewById(R.id.back_side);
        mTopSide = (CircularImageView) mBaseView.findViewById(R.id.top_side);
        mRightSide = (CircularImageView) mBaseView.findViewById(R.id.right_side);
        mUploadButton = (Button) mBaseView.findViewById(R.id.upload_button);
        uploadDetails = (TextView) mBaseView.findViewById(R.id.file_number);
        mProgressBar = (ProgressBar) mBaseView.findViewById(R.id.progressbar_Horizontal);
        progressLayout = (FrameLayout) mBaseView.findViewById(R.id.progress_layout);
        percentAge = (TextView) mBaseView.findViewById(R.id.percentage);
        mFrontSide.setOnClickListener(this);
        mRightSide.setOnClickListener(this);
        mTopSide.setOnClickListener(this);
        mBackSide.setOnClickListener(this);
        mLeftSide.setOnClickListener(this);
        mUploadButton.setOnClickListener(this);
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return mBaseView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.front_side:
                if (imagesHashMap.containsKey(requestCodes[0])) {
                    pressedButtonId = 0;
                    removeItemFromArray(requestCodes[0], mFrontSide);
                } else {
                    pressedButtonId = view.getId();
                    selectImage(requestCodes[0]);
                }
                break;
            case R.id.left_side:
                if (imagesHashMap.containsKey(requestCodes[1])) {
                    pressedButtonId = 0;
                    removeItemFromArray(requestCodes[1], mLeftSide);
                } else {
                    pressedButtonId = view.getId();
                    selectImage(requestCodes[1]);
//                    dispatchTakePictureIntent(requestCodes[1]);
                }
                break;
            case R.id.right_side:
                if (imagesHashMap.containsKey(requestCodes[2])) {
                    pressedButtonId = 0;
                    removeItemFromArray(requestCodes[2], mRightSide);
                } else {
                    pressedButtonId = view.getId();
                    selectImage(requestCodes[2]);
//                    dispatchTakePictureIntent(requestCodes[2]);
                }
                break;
            case R.id.top_side:
                if (imagesHashMap.containsKey(requestCodes[3])) {
                    pressedButtonId = 0;
                    removeItemFromArray(requestCodes[3], mTopSide);
                } else {
                    pressedButtonId = view.getId();
                    selectImage(requestCodes[3]);
//                    dispatchTakePictureIntent(requestCodes[3]);
                }
                break;
            case R.id.back_side:
                if (imagesHashMap.containsKey(requestCodes[4])) {
                    pressedButtonId = 0;
                    removeItemFromArray(requestCodes[4], mBackSide);
                } else {
                    pressedButtonId = view.getId();
                    selectImage(requestCodes[4]);
//
                }
                break;
            case R.id.upload_button:
//                MainActivity.loadFragment(new HealthInformation());
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.container, new HealthInformation());
//                fragmentTransaction.addToBackStack("Health_info");
//                fragmentTransaction.commit();
                if (imagesHashMap.size() < 5) {
                    Toast.makeText(getActivity(), "Please capture all the images", Toast.LENGTH_SHORT).show();
                } else {
                    if (!sUploaded) {
                        if (AppGlobals.sIsInternetAvailable) {
                            new CheckInternet(false).execute();
                        } else {
                            Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                                    executeTask(true));
                        }
                    } else {
//                        FragmentManager fragmentManager = getFragmentManager();
//                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                        fragmentTransaction.replace(R.id.container, new HealthInformation());
//                        fragmentTransaction.addToBackStack("Health_info");
//                        fragmentTransaction.commit();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container,new HealthInformation() , "health");
                        fragmentTransaction.addToBackStack(null); //this will add it to back stack
                        fragmentTransaction.commit();
                    }
                }
        }
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

    // Dialog with option to capture image or choose from gallery
    private void selectImage(final int id) {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    selectImage = false;
                    dispatchTakePictureIntent(id);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    selectImage = true;
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            id);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }


    private void removeItemFromArray(final int item, final CircularImageView circularImageView) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Discard Image");
        alertDialogBuilder.setMessage("Do you want to remove this image?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imagesHashMap.remove(item);
                circularImageView.setImageResource(0);
                switch (circularImageView.getId()) {
                    case R.id.front_side:
                        circularImageView.setImageResource(R.mipmap.front_side);
                        break;
                    case R.id.back_side:
                        circularImageView.setImageResource(R.mipmap.back_side);
                        break;
                    case R.id.top_side:
                        circularImageView.setImageResource(R.mipmap.top_side);
                        break;
                    case R.id.left_side:
                        circularImageView.setImageResource(R.mipmap.left_side);
                        break;
                    case R.id.right_side:
                        circularImageView.setImageResource(R.mipmap.right_side);
                        break;
                }
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public boolean contains(int[] array, final int key) {
        Arrays.sort(array);
        return Arrays.binarySearch(array, key) != -1;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (selectImage && resultCode == Activity.RESULT_OK) {
            Log.i("Consultation", "image selected");
            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.MediaColumns.DATA};
            CursorLoader cursorLoader = new CursorLoader(getActivity(), selectedImageUri, projection, null, null,
                    null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            if (!imagesHashMap.containsKey(requestCode)) {
                imagesHashMap.put(requestCode, selectedImagePath);
                setImageOnImageView(new File(selectedImagePath));
            }
            selectImage = false;

        }else if (contains(requestCodes, requestCode) && resultCode == Activity.RESULT_OK) {
            if (!imagesHashMap.containsKey(requestCode)) {
                Log.i("Consultation", "image captured");
                imagesHashMap.put(requestCode, filePath);
                setImageOnImageView(new File(filePath));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (contains(requestCodes, requestCode)) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();
                openCamera(requestCode);
            } else {
                Toast.makeText(getActivity(), "Permission denied!"
                        , Toast.LENGTH_LONG).show();
            }
            return;

        }
    }

    private void setImageOnImageView(File imageFile) {
        switch (pressedButtonId) {
            case R.id.front_side:
                setImage(mFrontSide, imageFile);
                break;
            case R.id.left_side:
                setImage(mLeftSide, imageFile);
                break;
            case R.id.right_side:
                setImage(mRightSide, imageFile);
                break;
            case R.id.top_side:
                setImage(mTopSide, imageFile);
                break;
            case R.id.back_side:
                setImage(mBackSide, imageFile);
                break;
        }
    }

    private void setImage(final CircularImageView image, File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        int newWidth = image.getWidth();
        int newHeight = (imageHeight * newWidth)/ imageWidth;
        Bitmap myBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        Bitmap orientedBitmap = RotateUtil.rotateBitmap(file.getAbsolutePath(), myBitmap);
        image.setImageBitmap(orientedBitmap);
    }


    private void dispatchTakePictureIntent(int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        } else {
            openCamera(requestCode);
        }
    }

    private void openCamera(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            filePath = Helpers.createDirectoryAndSaveFile();
            uriSavedImage = Uri.fromFile(new File(filePath));
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
            System.out.println(uriSavedImage);
            startActivityForResult(takePictureIntent, requestCode);
        }
    }

    private void uploadImages() {
        FormData data = new FormData();
        data.append(FormData.TYPE_CONTENT_TEXT, "user_id",
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_USER_ID));
        for (int i = 1; i < 6; i++) {
            data.append(FormData.TYPE_CONTENT_FILE,"image"+i , imagesHashMap.get(i));
        }
        mRequest = new HttpRequest(getActivity().getApplicationContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.setOnFileUploadProgressListener(this);
        mRequest.open("POST", AppGlobals.CONSULTATION_STEP_ONE);
        mRequest.send(data);
        progressLayout.setVisibility(View.VISIBLE);
        mUploadButton.setVisibility(View.GONE);
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int i) {
        JSONObject jsonObject;
        switch (i) {
            case HttpRequest.STATE_LOADING:
                progressLayout.setVisibility(View.GONE);
                mUploadButton.setVisibility(View.VISIBLE);
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Finishing up...");
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                break;
            case HttpRequest.STATE_DONE:
                progressDialog.dismiss();
                if (request != null) {
                    try {
                        jsonObject = new JSONObject(mRequest.getResponseText());
                        if (jsonObject.getString("Message").equals("Successfully")) {
                            JSONObject jsonDetails = jsonObject.getJSONObject("details");
                            AppGlobals.sEntryId = jsonDetails.getInt("entry_id");

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container, new HealthInformation());
                            fragmentTransaction.addToBackStack("Health_info");
                            fragmentTransaction.commit();


//                        startActivity(new Intent(getActivity().getApplicationContext(), HealthInformation.class));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onFileUploadProgress(HttpRequest httpRequest, File file, long l, long l1) {
        if (!uploaded.contains(file.getAbsolutePath())) {
            uploaded.add(file.getAbsolutePath());
            mProgressBar.setProgress(0);
        }
        uploadDetails.setText(uploaded.size() + "/" + imagesHashMap.size());
        double progress = (l / (double) l1) * 100;
        mProgressBar.setProgress((int) progress);
        percentAge.setText((int) progress + "/100");
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
            progressDialog.setMessage("Loading...");
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
                uploadImages();
            } else {
                Helpers.alertDialog(getActivity(), "No internet", "Please check your internet connection",
                        executeTask(true));
            }
        }
    }
}
