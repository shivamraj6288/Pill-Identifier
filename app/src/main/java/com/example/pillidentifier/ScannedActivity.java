package com.example.pillidentifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ScannedActivity extends AppCompatActivity {

    public CardView scanCard;
    private RecyclerView imageRecycler;
    public ArrayList<Bitmap> imageList;
    public ArrayList<Boolean> analysisList;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned);
        setViews();
        scanCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanIntent=new Intent(ScannedActivity.this, MainActivity.class);
                startActivity(scanIntent);
                finish();
            }
        });
        setRecyclerViewContent();
    }
    private void setViews(){
        scanCard=findViewById(R.id.goToScan);
        imageRecycler=findViewById(R.id.scannedImagesRecycler);

    }

    private void setRecyclerViewContent(){
        imageRecycler.setLayoutManager(new GridLayoutManager(this,2));
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] images=storageDir.listFiles();
        if(imageList!=null){
            imageList.clear();
        }
        if(analysisList!=null){
            analysisList.clear();
        }
        imageList=new ArrayList<Bitmap>();
        analysisList=new ArrayList<Boolean>();

        for (int i=0; i<images.length; i++){
            String name=images[i].getName();
            String check=name.substring(0,3);
            try {

                if (check.equals("YES")) {
                    imageList.add(BitmapFactory.decodeStream(new FileInputStream(images[i])));
                    analysisList.add(true);
                } else if (check.equals("NOO")) {
                    imageList.add(BitmapFactory.decodeStream(new FileInputStream(images[i])));
                    analysisList.add(false);
                }
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
        imageAdapter=new ImageAdapter(imageList,analysisList,this);
        imageRecycler.setAdapter(imageAdapter);
    }

}