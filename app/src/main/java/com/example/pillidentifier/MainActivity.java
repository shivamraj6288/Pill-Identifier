package com.example.pillidentifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private CardView cardView;
    private ProgressBar progressBar;
    public static final String TAG="MainActivity";
    public static final int REQUEST_IMAGE_CAPTURE=1;
    public Bitmap imageBitmap;
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
    private void setViews(){
        imageView=findViewById(R.id.takenimage);
        cardView=findViewById(R.id.rescan);
        progressBar=findViewById(R.id.progressbar);
    }
    private void takePicture(){

        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}