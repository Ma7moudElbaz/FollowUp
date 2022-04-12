package com.example.followup.requests.add;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.example.followup.R;
import com.example.followup.utils.UserUtils;
import com.example.followup.webservice.WebserviceContext;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductionActivity extends LocalizationActivity {

    EditText item_name, country, venue, days, delivery_date, quantity, dimensions, designer_in_charge, description, notes, screen_specs;
    Button choose_file, send_request;
    LinearLayout screen_specs_container;
    RadioGroup screen;
    ImageView back;
    private ProgressDialog dialog;
    DatePickerDialog picker;
    String screen_text = "yes";
    int projectId;

    List<String> filesSelected;
    private static final int FILES_REQUEST_CODE = 764546;
    TextView filesChosen;

    WebserviceContext ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_production);
        initFields();
        back.setOnClickListener(v -> onBackPressed());

        choose_file.setOnClickListener(v -> {

            Dexter.withContext(getBaseContext())
                    .withPermissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                        showFileChooser();
                        pickFromGallery();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                }
            }).check();
        });
        delivery_date.setOnClickListener(v -> showDatePicker(delivery_date));
        delivery_date.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker(delivery_date);
            }
        });

        screen.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.screen_yes:
                    screen_text = "Yes";
                    screen_specs_container.setVisibility(View.VISIBLE);
                    break;
                case R.id.screen_no:
                    screen_text = "No";
                    screen_specs_container.setVisibility(View.GONE);
                    break;
            }
        });

        send_request.setOnClickListener(v -> {
            if (validateFields()) {
                addProduction();
            }
        });
    }

    private void initFields() {
        ws = new WebserviceContext(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please, Wait...");
        dialog.setCancelable(false);

        filesSelected = new ArrayList<>();
        filesChosen = findViewById(R.id.files_chosen);

        projectId = getIntent().getIntExtra("project_id", 0);
        back = findViewById(R.id.back);
        item_name = findViewById(R.id.item_name);
        country = findViewById(R.id.country);
        venue = findViewById(R.id.venue);
        days = findViewById(R.id.days);
        delivery_date = findViewById(R.id.delivery_date);
        delivery_date.setInputType(InputType.TYPE_NULL);
        quantity = findViewById(R.id.quantity);
        dimensions = findViewById(R.id.dimensions);
        description = findViewById(R.id.description);
        notes = findViewById(R.id.notes);
        designer_in_charge = findViewById(R.id.designer_in_charge);
        screen = findViewById(R.id.screen);
        screen_specs = findViewById(R.id.screen_specs);
        screen_specs_container = findViewById(R.id.screen_specs_container);

        choose_file = findViewById(R.id.choose_file);
        send_request = findViewById(R.id.btn_send_request);
    }

    private boolean validateFields() {
        if (item_name.length() == 0) {
            item_name.setError("This is required field");
            return false;
        }
        if (country.length() == 0) {
            country.setError("This is required field");
            return false;
        }
        if (venue.length() == 0) {
            venue.setError("This is required field");
            return false;
        }
        if (days.length() == 0) {
            days.setError("This is required field");
            return false;
        }
        if (delivery_date.length() == 0) {
            delivery_date.setError("This is required field");
            return false;
        }
        if (quantity.length() == 0) {
            quantity.setError("This is required field");
            return false;
        }
        if (description.length() == 0) {
            description.setError("This is required field");
            return false;
        }
        if (notes.length() == 0) {
            notes.setError("This is required field");
            return false;
        }
        if (designer_in_charge.length() == 0) {
            designer_in_charge.setError("This is required field");
            return false;
        }
        if (screen_specs.length() == 0 && screen_text.equalsIgnoreCase("yes")) {
            screen_specs.setError("This is required field");
            return false;
        }
        return true;
    }

    private void addProduction() {
        Map<String, String> map = setProductionMap();

        dialog.show();
        ws.getApi().addRequest(UserUtils.getAccessToken(getBaseContext()), map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        JSONObject responseObject = new JSONObject(response.body().string());
                        if (filesSelected.size() != 0) {
                            addRequestAttaches(responseObject.getJSONObject("data").getString("id"));
                        } else {
                            dialog.dismiss();
                            Toast.makeText(getBaseContext(), "Request Added Successfully", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }

                    } else {
                        Toast.makeText(getBaseContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getBaseContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private Map<String, String> setProductionMap() {

        Map<String, String> map = new HashMap<>();
        map.put("type_id", "3");
        map.put("project_id", String.valueOf(projectId));
        map.put("item_name", item_name.getText().toString());
        map.put("country", country.getText().toString());
        map.put("venue", venue.getText().toString());
        map.put("days", days.getText().toString());
        map.put("delivery_date", delivery_date.getText().toString());
        map.put("quantity", quantity.getText().toString());
        map.put("dimension", dimensions.getText().toString());
        map.put("designer_name", designer_in_charge.getText().toString());
        map.put("description", description.getText().toString());
        map.put("note", notes.getText().toString());
        map.put("screen", screen_specs.getText().toString());

        return map;
    }

    private void showDatePicker(TextView textview) {

        final Calendar cldr = Calendar.getInstance();
        // date picker dialog
        picker = new DatePickerDialog(AddProductionActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {

                    cldr.set(Calendar.YEAR, year);
                    cldr.set(Calendar.MONTH, monthOfYear);
                    cldr.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String myFormat = "yy-MM-dd"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    textview.setText(sdf.format(cldr.getTime()));
                }, cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH),
                cldr.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }


    public void addRequestAttaches(final String requestId) {
        dialog.show();

        List<MultipartBody.Part> fileToUpload = addAttaches(filesSelected);
        RequestBody request_id = RequestBody.create(MediaType.parse("text/plain"), requestId);

        ws.getApi().addAttach(UserUtils.getAccessToken(getBaseContext()), fileToUpload, request_id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        JSONObject res = new JSONObject(response.body().string());
                        Toast.makeText(getBaseContext(), "Request Added successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getBaseContext(), "Request Added successfully but attachment failed", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                onBackPressed();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getBaseContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                Log.d("Request failure", call.toString() + " , " + t.getMessage());
                dialog.dismiss();
                onBackPressed();
            }
        });
    }

    private void pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // Launching the Intent
        startActivityForResult(intent, FILES_REQUEST_CODE);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILES_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    private List<MultipartBody.Part> addAttaches(List<String> files) {
        List<MultipartBody.Part> list = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            File file = new File(files.get(i));
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("attaches[]", file.getName(), fileReqBody);
            list.add(part);
        }
        return list;
    }

    public static String getRealPath(Context context, Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case FILES_REQUEST_CODE:
                    filesSelected.clear();
                    //data.getData returns the content URI for the selected files
                    if (data == null) {
                        return;
                    } else if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            filesSelected.add(getRealPath(getBaseContext(), uri));
                        }
                    } else {

                        Uri uri = data.getData();
//                        filesSelected.add(getPath(getBaseContext(), uri));
                        filesSelected.add(getRealPath(getBaseContext(), uri));
                        Log.e("Single", getRealPath(getBaseContext(), uri));
                    }
                    filesChosen.setText(filesSelected.size() + " Files Selected");

                    Log.e("Data selected", filesSelected.toString());
            }
    }
}