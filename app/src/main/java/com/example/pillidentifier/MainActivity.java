package com.example.pillidentifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public ImageView imageView,correct,wrong;
    public CardView cardView, goToScanned;
    public ProgressBar progressBar;//, uploadProgressBar;
    public static final String TAG="MainActivity";
    public static final int REQUEST_IMAGE_CAPTURE=1;
    public Bitmap imageBitmap;
    public String receivedB64;

    public File photoFile;

    public String imageCode;
    public static String URL="http://54.165.240.234:8000/uploadimage/";
    public String picturePath;
    int nlabel;
//    byte[] ba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();

        cardView.setOnClickListener(v -> {
            imageView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            correct.setVisibility(View.INVISIBLE);
            wrong.setVisibility(View.INVISIBLE);
//            takePicture();
            dispatchTakePictureIntent();
        });


//        takePicture();
        dispatchTakePictureIntent();
        goToScanned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ScannedActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
        photoFile.delete();
        new uploadToServer().execute();
    }
    private void upload3(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        imageCode= Base64.encodeToString(byteArray, Base64.DEFAULT);
        new uploadToServer().execute();
    }

    private File createImageFile2(String result) throws IOException {
        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
//        String timeStamp="111";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp +"_"+result+"_";
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

    private boolean saveImage(Bitmap bitmap, int countLabel){
        FileOutputStream fileOutputStream=null;
        File currentImage=null;
        if (countLabel>0){
            try {
                currentImage=createImageFile2("YES");
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return false;
            }
        }
        else {
            try {
                currentImage = createImageFile2("NOO");
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return false;
            }
        }
            try {
                fileOutputStream = new FileOutputStream(currentImage);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;

    }
    private void setViews(){
        imageView=findViewById(R.id.takenimage);
        cardView=findViewById(R.id.rescan);
        progressBar=findViewById(R.id.progressbar);
//        uploadProgressBar=findViewById(R.id.uploadBar);
        correct=findViewById(R.id.correct);
        wrong=findViewById(R.id.wrong);
        goToScanned=findViewById(R.id.goToScanned);
    }

    private void takePicture(){

        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
        try{
            if(takePictureIntent.resolveActivity(getPackageManager())!=null){
                photoFile=null;
                try{
                    photoFile=createImageFile2("jaja");
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
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            Toast.makeText(MainActivity.this,"Image Capture Failed",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {

            Bundle extras=data.getExtras();
            imageBitmap=(Bitmap) extras.get("data");
//            setPic2();
            nlabel=0;
//            imageView.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.INVISIBLE);

//            Toast.makeText(MainActivity.this,"Image ok",Toast.LENGTH_SHORT).show();
            upload3(imageBitmap);


        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void setPic2() {
//        imageBitmap= BitmapFactory.decodeFile(picturePath);
//        photoFile.delete();

        imageView.setImageBitmap(imageBitmap);

    }

    public class uploadToServer extends AsyncTask<Void,Void,String>{

        protected void onPreExecute(){
            super.onPreExecute();
            cardView.setVisibility(View.INVISIBLE);
            goToScanned.setVisibility(View.INVISIBLE);
//            uploadProgressBar.setVisibility(View.VISIBLE);
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
//                            uploadProgressBar.setVisibility(View.INVISIBLE);
                            cardView.setVisibility(View.VISIBLE);
                            goToScanned.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                String checkResult;
                                if (response.isSuccessful()){
                                    checkResult="true";
                                }
                                else {
                                    checkResult="false";
                                }
                                Log.e(TAG,"response : "+checkResult);
                                if(response.isSuccessful()){

                                    final String responseList=response.body().string();
                                    Log.e(TAG, "check---"+responseList);
                                    JSONObject jsonObject=new JSONObject(responseList);


                                    String result=jsonObject.getString("result");
                                    if (result.isEmpty() || !result.equals("success")){
                                        Toast.makeText(MainActivity.this,"data is in wrong format",Toast.LENGTH_LONG).show();
//                                        uploadProgressBar.setVisibility(View.INVISIBLE);
                                        cardView.setVisibility(View.VISIBLE);
                                        goToScanned.setVisibility(View.VISIBLE);
                                        imageView.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);

                                    }
                                    else {
                                        Toast.makeText(MainActivity.this,"Image Analysis Completed",Toast.LENGTH_SHORT).show();
                                        rs[0] ="success";
                                        nlabel=jsonObject.getInt("nlabel");
//                                        receivedB64=jsonObject.getString("b64code");
//                                        Log.e(TAG,receivedB64);
//                                        byte[] decodedString=Base64.decode(receivedB64,Base64.DEFAULT);
//                                        imageBitmap=BitmapFactory.decodeByteArray(decodedString,0, decodedString.length);
                                        JSONArray cords=jsonObject.getJSONArray("cord");
                                        Paint myRectPaint=new Paint();
                                        myRectPaint.setColor(Color.rgb(255,0,50));
                                        Bitmap tempBitmap=Bitmap.createBitmap(imageBitmap.getWidth(),imageBitmap.getHeight(),Bitmap.Config.RGB_565);
                                        Canvas tempCanvas=new Canvas(tempBitmap);
                                        tempCanvas.drawBitmap(imageBitmap,0,0,null);
                                        for (int i=0; i<cords.length(); i=i+4){
                                            float x1=(float)(imageBitmap.getWidth()*cords.getDouble(i));
                                            float y1=(float)(imageBitmap.getHeight()*cords.getDouble(i+1));
                                            float x2=(float)(imageBitmap.getWidth()*cords.getDouble(i+2));
                                            float y2=(float)(imageBitmap.getHeight()*cords.getDouble(i+3));
//                                            tempCanvas.drawRoundRect(new RectF(x1,y1,x2,y2),2,2,myRectPaint);
                                            tempCanvas.drawLine(x1,y1,x1+x2,y1,myRectPaint);
                                            tempCanvas.drawLine(x1+x2,y1,x1+x2,y1+y2,myRectPaint);
                                            tempCanvas.drawLine(x1+x2,y1+y2,x1,y1+y2,myRectPaint);
                                            tempCanvas.drawLine(x1,y1+y2,x1,y1,myRectPaint);
//                                            tempCanvas.drawRect(new RectF(x1,y1,x2,y2),myRectPaint);
//                                            tempCanvas.drawRect(x1,y1,x2,y2,myRectPaint);
                                            Log.e(TAG,"check cord---"+String.valueOf(x1)+" "+String.valueOf(y1)+" "+String.valueOf(x2)+" "+String.valueOf(y2));
//                                            break;
                                        }
//                                        setPic2();
                                        imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
                                        imageView.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
//                                        uploadProgressBar.setVisibility(View.INVISIBLE);
                                        cardView.setVisibility(View.VISIBLE);
                                        goToScanned.setVisibility(View.VISIBLE);
                                        if(nlabel>0){
                                            correct.setVisibility(View.VISIBLE);
                                        }
                                        else{
                                            wrong.setVisibility(View.VISIBLE);
                                        }
                                        saveImage(tempBitmap,nlabel);

                                    }



                                }
                            }
                            catch (Exception e){
                                Toast.makeText(MainActivity.this,e.getMessage() + " on Response Catch Error",Toast.LENGTH_LONG).show();
                                imageView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
//                                uploadProgressBar.setVisibility(View.INVISIBLE);
                                cardView.setVisibility(View.VISIBLE);
                                goToScanned.setVisibility(View.VISIBLE);
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
//                    uploadProgressBar.setVisibility(View.INVISIBLE);
//                    cardView.setVisibility(View.VISIBLE);


                }
            });

        }
    }
}