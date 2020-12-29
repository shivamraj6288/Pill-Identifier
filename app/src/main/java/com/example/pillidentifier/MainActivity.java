package com.example.pillidentifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public ImageView imageView;
    public CardView cardView;
    public ProgressBar progressBar, uploadProgressBar;
    public static final String TAG="MainActivity";
    public static final int REQUEST_IMAGE_CAPTURE=1;
    public Bitmap imageBitmap;

    public Uri selectedImage, fileUri;

    public String imageCode;
    public static String URL="http://54.165.240.234:8000/uploadimage/";
    public String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                takePicture();
            }
        });


        takePicture();


    }
    private void upload(){
        Log.e("path","-----------"+picturePath);

        Bitmap bitmapCopy=imageBitmap;
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmapCopy.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] ba=byteArrayOutputStream.toByteArray();
        imageCode =Base64.encodeToString(ba,Base64.NO_WRAP);

        Log.e("base64", "------"+ imageCode);

//        cardView.setVisibility(View.GONE);
//        uploadProgressBar.setVisibility(View.VISIBLE);
//
//        final String[] rs = {"fail"};
//        OkHttpClient client=new OkHttpClient();
//        RequestBody formBody=new FormBody.Builder()
//                .add("file",bal)
//                .build();
//        final Request request=new Request.Builder().url(URL)
//                .post(formBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
//                        Log.d(TAG+" upload", e.getMessage());
//                        uploadProgressBar.setVisibility(View.GONE);
//                        cardView.setVisibility(View.VISIBLE);
//                    }
//                });
//
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//                            if(response.isSuccessful()){
//                                final String responseList=response.body().string();
//                                JSONObject jsonObject=new JSONObject(responseList);
//
//                                String result=jsonObject.getString("result");
//                                if (result.isEmpty() || !result.equals("success")){
//                                    Toast.makeText(MainActivity.this,responseList,Toast.LENGTH_LONG).show();
//                                    uploadProgressBar.setVisibility(View.GONE);
//                                    cardView.setVisibility(View.VISIBLE);
//                                }
//                                else {
//                                    Toast.makeText(MainActivity.this,"balle balle",Toast.LENGTH_LONG).show();
//                                    rs[0] ="success";
//                                    uploadProgressBar.setVisibility(View.GONE);
//                                    cardView.setVisibility(View.VISIBLE);
//
//                                }
//
//
//
//                            }
//                        }
//                        catch (Exception e){
//                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
//                            uploadProgressBar.setVisibility(View.GONE);
//                            cardView.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//            }
//        });


        new uploadToServer().execute();

    }
    private void setViews(){
        imageView=findViewById(R.id.takenimage);
        cardView=findViewById(R.id.rescan);
        progressBar=findViewById(R.id.progressbar);
        uploadProgressBar=findViewById(R.id.uploadBar);
    }

    private void takePicture(){

        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
        try{
            if(takePictureIntent.resolveActivity(getPackageManager())!=null){
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
        catch (ActivityNotFoundException e){
            Toast.makeText(MainActivity.this,"Image Capture Failed",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {

            selectedImage=data.getData();
            imageBitmap=(Bitmap)data.getExtras().get("data");

//            String[] filePatColumn={MediaStore.Images.Media.DATA};
//            Cursor cursor =getContentResolver().query(selectedImage,filePatColumn,null,null,null);
//            cursor.moveToFirst();

//            int columnIndex=cursor.getColumnIndex(filePatColumn[0]);
//            picturePath=cursor.getString(columnIndex);
//            cursor.close();

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this,"Image ok",Toast.LENGTH_SHORT).show();
            upload();


        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public class uploadToServer extends AsyncTask<Void,Void,String>{

        protected void onPreExecute(){
            super.onPreExecute();
            cardView.setVisibility(View.GONE);
            uploadProgressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... voids) {
            final String[] rs = {"fail"};
            OkHttpClient client=new OkHttpClient();
            RequestBody formBody=new FormBody.Builder()
                    .add("imageData", imageCode)
                    .build();
            final Request request=new Request.Builder().url(URL)
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            Log.d(TAG+" upload", e.getMessage());
                            uploadProgressBar.setVisibility(View.GONE);
                            cardView.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(response.isSuccessful()){
                                    final String responseList=response.body().string();
                                    JSONObject jsonObject=new JSONObject(responseList);

                                    String result=jsonObject.getString("result");
                                    if (result.isEmpty() || !result.equals("success")){
                                        Toast.makeText(MainActivity.this,responseList,Toast.LENGTH_LONG).show();
                                        uploadProgressBar.setVisibility(View.GONE);
                                        cardView.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this,"balle balle",Toast.LENGTH_LONG).show();
                                        rs[0] ="success";
                                        uploadProgressBar.setVisibility(View.GONE);
                                        cardView.setVisibility(View.VISIBLE);

                                    }



                                }
                            }
                            catch (Exception e){
                                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                uploadProgressBar.setVisibility(View.GONE);
                                cardView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            });
            return rs[0];
        }


        protected void onPostExecute(String result){
            super.onPostExecute(result);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uploadProgressBar.setVisibility(View.GONE);
                    cardView.setVisibility(View.VISIBLE);
                }
            });

        }
    }
}