package com.example.pdftoserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final int PICK_PDF_FILE = 2;
    private int STORAGE_PERMISSION_CODE = 1;

    TextView txt_fileName;
    Button btn_filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_fileName = (TextView) findViewById(R.id.fileName);
        btn_filePicker = (Button)findViewById(R.id.pickFile);

        btn_filePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You have already granted this permission!",
                            Toast.LENGTH_SHORT).show();

                    Intent myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    myFileIntent.setType("*/*");
                    startActivityForResult(myFileIntent,10);
                } else {
                    requestStoragePermission();
                }
           }
        });



        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        }

        Button uploadButton = (Button) findViewById(R.id.btn_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_PDF_FILE);
            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_PDF_FILE && requestCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            uploadFile(uri);
        }

        switch (requestCode)
        {
            case 10:
                if(resultCode==RESULT_OK)
                {

                    String path = data.getData().getPath();
                    String fileName=path.substring(path.lastIndexOf("/")+1);
                    txt_fileName.setText(fileName);

                }
        }

    }

            @Override
            public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

            if (requestCode == STORAGE_PERMISSION_CODE)  {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                    }
                }
                switch (requestCode) {

                    case MY_PERMISSIONS_REQUEST: {
                        //If request is cancelled, the result arrays are empty.
                        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                            // Permission was granted, file-related task you need to do.
                        }
                        else {
                            // Permission denied
                        }
                        return;
                    }
                }
            }



        private void uploadFile(Uri fileUri) {

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("http://gyankosh.live/")
                    .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();
            UploadPdf client = retrofit.create(UploadPdf.class);

            File originalFile = new File(fileUri.getPath());

            RequestBody filePart = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(fileUri)),
                    originalFile);


            MultipartBody.Part file =
                    MultipartBody.Part.createFormData("PDF", originalFile.getName(), filePart);

          // execute the request
            Call<ResponseBody> call = client.uploadPdf(file);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Upload Error!!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }