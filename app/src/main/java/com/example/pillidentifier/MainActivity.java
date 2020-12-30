package com.example.pillidentifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public File photoFile;

    public String imageCode;
    public static String URL="http://54.165.240.234:8000/uploadimage/";
    public String picturePath;
//    byte[] ba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();

        cardView.setOnClickListener(v -> {
            imageView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            takePicture();
        });


        takePicture();
    }
    private void upload(){
        Log.e("path","-----------"+picturePath);

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
        byte[] ba=byteArrayOutputStream.toByteArray();
        imageCode =Base64.encodeToString(ba,Base64.NO_WRAP);

        Log.e("base64", "------"+ imageCode);



        try {
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new uploadToServer().execute();


    }
    private void upload2(){
        InputStream inputStream = null; // You can get an inputStream using any I/O API
        try {
            inputStream = new FileInputStream(picturePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("read","-----------error, can't read");
            Log.e("read",e.getMessage());
            return;
        }
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        bytes = output.toByteArray();
        imageCode= Base64.encodeToString(bytes, Base64.DEFAULT);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        photoFile.delete();
        new uploadToServer().execute();
    }

    private File createImageFile2() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
//        String timeStamp="111";
        String imageFileName = "JPEG_" + timeStamp +"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        picturePath = image.getAbsolutePath();
        return image;
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
                photoFile=null;
                try{
                    photoFile=createImageFile2();
                }
                catch (IOException e){
                    Log.e(TAG+" image",e.getMessage());
                }
                if (photoFile!=null){
                    Uri photoUri=FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                }
                else {
                    Toast.makeText(MainActivity.this,"PhotoFile not created", Toast.LENGTH_LONG).show();
                    Log.d(TAG+" image", "photoFile not created");
                }

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

            setPic2();
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this,"Image ok",Toast.LENGTH_SHORT).show();
            upload2();


        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void setPic2() {
        imageBitmap= BitmapFactory.decodeFile(picturePath);
//        photoFile.delete();
        imageView.setImageBitmap(imageBitmap);

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
//            imageCode =Base64.encodeToString(ba,Base64.NO_WRAP);
//            Log.e("base64", "------"+ imageCode);
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