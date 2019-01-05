package com.hamaar.meetapresto.Activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hamaar.meetapresto.ImagePicker.RxImageConverters;
import com.hamaar.meetapresto.ImagePicker.RxImagePicker;
import com.hamaar.meetapresto.ImagePicker.Sources;
import com.hamaar.meetapresto.Model.Category;
import com.hamaar.meetapresto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static com.hamaar.meetapresto.Utils.GlobalHelper.compressFoto;
import static com.hamaar.meetapresto.Utils.GlobalHelper.convertFileToContentUri;
import static com.hamaar.meetapresto.Utils.GlobalHelper.encodeFileBase64;
import static com.hamaar.meetapresto.Utils.GlobalHelper.getMimeTypeFromUri;
import static com.hamaar.meetapresto.Utils.GlobalHelper.getPath;
import static com.hamaar.meetapresto.Utils.GlobalVars.BASE_DIR;
import static com.hamaar.meetapresto.Utils.GlobalVars.BASE_IP;
import static com.hamaar.meetapresto.Utils.GlobalVars.EXTERNAL_DIR_FILES;

public class InsertMenuActivity extends AppCompatActivity {

    //OPEN CAMERA
    private RadioGroup converterRadioGroup;
    private Uri photoUri = null;
    private Uri finalPhotoUri = null;
    private File compressedImage = null;
    private File tempFile = null;
    private String photoExt = "";
    private String encodePhoto = "";
    private String fotoTimeStamp;

    private Button btnInputOrder;
    private ImageView ivCamera;
    private ImageView ivFile;
    private TextView tvPlaceholder;
    private ImageView ivDeleteImage;
    private ImageView ivImage;
    private FrameLayout framePhoto;
    private EditText txtPrice;
    private EditText txtDesc;
    private EditText txtName;
    private Spinner spinCategory;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_menu);

        btnInputOrder = findViewById(R.id.btnSendMessage);
        ivCamera = findViewById(R.id.ivCamera);
        ivFile = findViewById(R.id.ivFile);
        tvPlaceholder = findViewById(R.id.tvPlaceholder);
        ivDeleteImage = findViewById(R.id.ivDeleteImage);
        ivImage = findViewById(R.id.ivImage);
        framePhoto = findViewById(R.id.frameFoto);
        txtPrice = findViewById(R.id.txtPrice);
        txtDesc = findViewById(R.id.txtDesc);
        txtName = findViewById(R.id.txtName);
        spinCategory = findViewById(R.id.spinCategory);

        converterRadioGroup = findViewById(R.id.radio_group);
        converterRadioGroup.check(R.id.radio_file);
        if (RxImagePicker.with(this).getActiveSubscription() != null) {
            RxImagePicker.with(this).getActiveSubscription().subscribe(this::onImagePicked);
        }


        ivFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromSource(Sources.GALLERY);
            }
        });

        ivCamera.setOnClickListener(new View.
                OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromSource(Sources.CAMERA);
            }
        });

        ivDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivDeleteImage.setVisibility(View.GONE);
                tvPlaceholder.setVisibility(View.VISIBLE);
                encodePhoto = null;
                photoExt = null;
                Glide.with(InsertMenuActivity.this).load("")
                        .into(ivImage);
            }
        });

        getCategory();
    }


    private void pickImageFromSource(Sources source) {


        RxImagePicker.with(InsertMenuActivity.this).requestImage(source)
                .flatMap(uri -> {
                    switch (converterRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.radio_file:
                            return RxImageConverters.uriToFile(InsertMenuActivity.this, uri, createTempFile());
                        case R.id.radio_bitmap:
                            return RxImageConverters.uriToBitmap(InsertMenuActivity.this, uri);
                        default:
                            return Observable.just(uri);
                    }
                })
                .subscribe(this::onImagePicked, throwable -> Toast.makeText(InsertMenuActivity.this, String.format("Error: %s", throwable), Toast.LENGTH_LONG).show());
    }

    private File createTempFile() {
        return new File(BASE_DIR + EXTERNAL_DIR_FILES, fotoTimeStamp + ".jpeg");
    }

    private void onImagePicked(Object result) {
        if (result instanceof Bitmap) {
            ivImage.setImageBitmap((Bitmap) result);
        }else{
            photoUri = Uri.parse(String.valueOf(result));

            tempFile = new File(String.valueOf(photoUri));

            compressedImage = compressFoto(InsertMenuActivity.this, tempFile);

            try {
                finalPhotoUri = convertFileToContentUri(InsertMenuActivity.this, compressedImage);

                tvPlaceholder.setVisibility(View.GONE);
                ivDeleteImage.setVisibility(View.VISIBLE);

                Glide.with(this).load(finalPhotoUri)
                        .into(ivImage);

                photoExt = getMimeTypeFromUri(InsertMenuActivity.this, finalPhotoUri);
                encodePhoto = encodeFileBase64(getPath(InsertMenuActivity.this, finalPhotoUri));

                //Log.e("encodePhoto", String.valueOf(encodePhoto));
                //Log.e("photoExt", String.valueOf(photoExt));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //createTempFile().getCanonicalFile().delete();
            //tempFile.getCanonicalFile().delete();
        }
    }

    private void getCategory(){
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.please_wait_onprocess));
        progress.setTitle(getString(R.string.please_wait_onprocess));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

        AndroidNetworking.post(BASE_IP + "category/readAll")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        List<Category> results = new ArrayList<>();

                        if (results!=null)
                            results.clear();

                        try {
                            if (response.getString("message").equals("success")){
                                String records = response.getString("records");
                                JSONArray dataArr = new JSONArray(records);

                                if (dataArr.length()>0){
                                    for (int i = 0; i < dataArr.length(); i++) {
                                        Category fromJson = gson.fromJson(dataArr.getJSONObject(i).toString(), Category.class);
                                        results.add(fromJson);

                                    }

                                    setCategory(results);
                                }

                                progress.dismiss();
                            }else{
                                progress.dismiss();
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progress.dismiss();
                            Toast.makeText(getApplicationContext(), "JSONException "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), "ANError "+error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void  setCategory(List<Category> categoryList){

        Category category = new Category(-1, getString(R.string.category));
        categoryList.add(0, category);

        ArrayAdapter<Category> voteTypeAdapter = new ArrayAdapter<Category>(getApplicationContext(), R.layout.item_spinner, categoryList){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);

                textView.setText(categoryList.get(position).getCat());

                if (position==0){
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                }else{
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_700));
                }


                return  textView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);

                textView.setText(categoryList.get(position).getCat());

                if (position==0){
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                }else{
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_700));
                }
                return textView;
            }
        };
        spinCategory.setAdapter(voteTypeAdapter);

        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {

            }
        });

    }



}
